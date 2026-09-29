import { useEffect, useMemo, useState } from 'react'
import type { FormEvent } from 'react'
import './App.css'

type Role = 'ATTENDEE' | 'ORGANIZER'
type EventStatus = 'DRAFT' | 'PUBLISHED' | 'SOLD_OUT' | 'COMPLETED' | 'CANCELLED'
type User = { id: number; nom: string; prenom: string; email: string; role: Role }
type BookingEvent = { id: number; title: string; description: string; date: string; venue: string; capacity: number; status: EventStatus; organizerId: number; organizerName: string }
type TicketType = { id: number; name: string; price: number; quantityAvailable: number; eventId: number }
type Booking = { id: number; status: 'PENDING' | 'CONFIRMED' | 'CANCELLED'; quantity: number; bookedAt: string; userId: number; ticketTypeId: number; ticketTypeName: string }

const apiUrl = import.meta.env.VITE_API_URL ?? ''
const dateFormatter = new Intl.DateTimeFormat('en', { weekday: 'short', month: 'short', day: 'numeric', year: 'numeric', hour: 'numeric', minute: '2-digit' })
const moneyFormatter = new Intl.NumberFormat('en', { style: 'currency', currency: 'USD' })

async function api<T>(path: string, options: RequestInit = {}, token?: string): Promise<T> {
  const response = await fetch(`${apiUrl}${path}`, {
    ...options,
    headers: { ...(options.body ? { 'Content-Type': 'application/json' } : {}), ...(token ? { Authorization: `Bearer ${token}` } : {}), ...options.headers },
  })
  if (!response.ok) {
    const error = await response.json().catch(() => null) as { message?: string } | null
    throw new Error(error?.message || 'Something went wrong. Please try again.')
  }
  return response.json() as Promise<T>
}

function formatDate(date: string) {
  return dateFormatter.format(new Date(date))
}

function App() {
  const [token, setToken] = useState(() => localStorage.getItem('booking-token') ?? '')
  const [currentUser, setCurrentUser] = useState<User | null>(() => {
    const saved = localStorage.getItem('booking-user')
    return saved ? JSON.parse(saved) as User : null
  })
  const [events, setEvents] = useState<BookingEvent[]>([])
  const [selectedEvent, setSelectedEvent] = useState<BookingEvent | null>(null)
  const [tickets, setTickets] = useState<TicketType[]>([])
  const [bookings, setBookings] = useState<Booking[]>([])
  const [search, setSearch] = useState('')
  const [selectedStatus, setSelectedStatus] = useState<'ALL' | EventStatus>('PUBLISHED')
  const [notice, setNotice] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const [showAuth, setShowAuth] = useState(!token)
  const [showCreateEvent, setShowCreateEvent] = useState(false)
  const [activeView, setActiveView] = useState<'discover' | 'bookings' | 'manage'>('discover')
  const [managedEvent, setManagedEvent] = useState<BookingEvent | null>(null)
  const [managedTickets, setManagedTickets] = useState<TicketType[]>([])
  const [editingEvent, setEditingEvent] = useState<BookingEvent | null>(null)
  const [editingTicket, setEditingTicket] = useState<TicketType | null>(null)
  const [showTicketForm, setShowTicketForm] = useState(false)
  const [profileMenuOpen, setProfileMenuOpen] = useState(false)

  const filteredEvents = useMemo(() => {
    const query = search.trim().toLowerCase()
    return events.filter((event) => {
      const matchesSearch = !query || [event.title, event.venue, event.description].some((value) => value?.toLowerCase().includes(query))
      return matchesSearch && (selectedStatus === 'ALL' || event.status === selectedStatus)
    })
  }, [events, search, selectedStatus])

  function persistSession(nextToken: string, user: User) {
    localStorage.setItem('booking-token', nextToken)
    localStorage.setItem('booking-user', JSON.stringify(user))
    setToken(nextToken)
    setCurrentUser(user)
  }

  async function loadEvents(sessionToken = token) {
    if (!sessionToken) return
    setLoading(true)
    setError('')
    try {
      setEvents(await api<BookingEvent[]>('/api/events', {}, sessionToken))
    } catch (requestError) {
      setError(requestError instanceof Error ? requestError.message : 'Unable to load events.')
    } finally {
      setLoading(false)
    }
  }

  async function loadBookings(sessionToken = token, user = currentUser) {
    if (!sessionToken || !user) return
    try {
      setBookings(await api<Booking[]>(`/api/bookings?userId=${user.id}`, {}, sessionToken))
    } catch (requestError) {
      setError(requestError instanceof Error ? requestError.message : 'Unable to load bookings.')
    }
  }

  useEffect(() => {
    void loadEvents()
    void loadBookings()
  }, [token])

  async function openEvent(event: BookingEvent) {
    setSelectedEvent(event)
    setTickets([])
    setError('')
    try {
      setTickets(await api<TicketType[]>(`/api/ticket-types?eventId=${event.id}`, {}, token))
    } catch (requestError) {
      setError(requestError instanceof Error ? requestError.message : 'Unable to load ticket options.')
    }
  }

  async function handleAuth(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const form = new FormData(event.currentTarget)
    const mode = String(form.get('mode'))
    const email = String(form.get('email')).trim()
    const password = String(form.get('password'))
    setError('')
    setLoading(true)
    try {
      if (mode === 'register') {
        await api<User>('/api/users', { method: 'POST', body: JSON.stringify({ nom: String(form.get('nom')).trim(), prenom: String(form.get('prenom')).trim(), email, password, role: String(form.get('role')) }) })
      }
      const login = await api<{ token: string }>('/api/auth/login', { method: 'POST', body: JSON.stringify({ email, password }) })
      const users = await api<User[]>('/api/users')
      const user = users.find((candidate) => candidate.email.toLowerCase() === email.toLowerCase())
      if (!user) throw new Error('Your account was created, but could not be loaded. Please sign in again.')
      persistSession(login.token, user)
      setShowAuth(false)
      setNotice(mode === 'register' ? 'Your account is ready. Welcome to Evently.' : `Welcome back, ${user.prenom}.`)
      await loadEvents(login.token)
      await loadBookings(login.token, user)
    } catch (requestError) {
      setError(requestError instanceof Error ? requestError.message : 'Unable to sign in.')
    } finally {
      setLoading(false)
    }
  }

  async function createBooking(ticket: TicketType, quantity: number) {
    if (!currentUser) {
      setShowAuth(true)
      return
    }
    setLoading(true)
    setError('')
    try {
      await api<Booking>(`/api/bookings?userId=${currentUser.id}&ticketTypeId=${ticket.id}&quantity=${quantity}`, { method: 'POST' }, token)
      setNotice(`${quantity} ${ticket.name} ticket${quantity > 1 ? 's are' : ' is'} reserved.`)
      setSelectedEvent(null)
      await loadEvents()
      await loadBookings()
    } catch (requestError) {
      setError(requestError instanceof Error ? requestError.message : 'Unable to create booking.')
    } finally {
      setLoading(false)
    }
  }

  async function createEvent(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!currentUser) return
    const form = new FormData(event.currentTarget)
    setLoading(true)
    setError('')
    try {
      await api<BookingEvent>(`/api/events?organizerId=${currentUser.id}`, { method: 'POST', body: JSON.stringify({ title: String(form.get('title')).trim(), description: String(form.get('description')).trim(), date: String(form.get('date')), venue: String(form.get('venue')).trim(), capacity: Number(form.get('capacity')), status: String(form.get('status')) }) }, token)
      setNotice('Event published. Add ticket types through the backend for now.')
      setShowCreateEvent(false)
      await loadEvents()
    } catch (requestError) {
      setError(requestError instanceof Error ? requestError.message : 'Unable to publish event.')
    } finally {
      setLoading(false)
    }
  }

  async function updateEvent(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!currentUser || !editingEvent) return
    const form = new FormData(event.currentTarget)
    setLoading(true)
    setError('')
    try {
      await api<BookingEvent>(`/api/events/${editingEvent.id}?organizerId=${currentUser.id}`, { method: 'PUT', body: JSON.stringify({ title: String(form.get('title')).trim(), description: String(form.get('description')).trim(), date: String(form.get('date')), venue: String(form.get('venue')).trim(), capacity: Number(form.get('capacity')), status: String(form.get('status')) }) }, token)
      setNotice('Event details updated.')
      setEditingEvent(null)
      await loadEvents()
    } catch (requestError) {
      setError(requestError instanceof Error ? requestError.message : 'Unable to update event.')
    } finally {
      setLoading(false)
    }
  }

  async function deleteEvent(event: BookingEvent) {
    if (!currentUser) return
    setLoading(true)
    setError('')
    try {
      await api<void>(`/api/events/${event.id}?organizerId=${currentUser.id}`, { method: 'DELETE' }, token)
      setNotice('Event deleted.')
      setManagedEvent(null)
      setManagedTickets([])
      await loadEvents()
    } catch (requestError) {
      setError(requestError instanceof Error ? requestError.message : 'Unable to delete event.')
    } finally {
      setLoading(false)
    }
  }

  async function selectManagedEvent(event: BookingEvent) {
    setManagedEvent(event)
    setManagedTickets([])
    setError('')
    try {
      setManagedTickets(await api<TicketType[]>(`/api/ticket-types?eventId=${event.id}`, {}, token))
    } catch (requestError) {
      setError(requestError instanceof Error ? requestError.message : 'Unable to load ticket types.')
    }
  }

  async function saveTicket(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!currentUser || !managedEvent) return
    const form = new FormData(event.currentTarget)
    const body = JSON.stringify({ name: String(form.get('name')).trim(), price: Number(form.get('price')), quantityAvailable: Number(form.get('quantityAvailable')) })
    setLoading(true)
    setError('')
    try {
      const path = editingTicket
        ? `/api/ticket-types/${editingTicket.id}?organizerId=${currentUser.id}`
        : `/api/ticket-types?eventId=${managedEvent.id}&organizerId=${currentUser.id}`
      await api<TicketType>(path, { method: editingTicket ? 'PUT' : 'POST', body }, token)
      setNotice(editingTicket ? 'Ticket type updated.' : 'Ticket type added.')
      setEditingTicket(null)
      setShowTicketForm(false)
      await selectManagedEvent(managedEvent)
    } catch (requestError) {
      setError(requestError instanceof Error ? requestError.message : 'Unable to save ticket type.')
    } finally {
      setLoading(false)
    }
  }

  async function deleteTicket(ticket: TicketType) {
    if (!currentUser || !managedEvent) return
    setLoading(true)
    setError('')
    try {
      await api<void>(`/api/ticket-types/${ticket.id}?organizerId=${currentUser.id}`, { method: 'DELETE' }, token)
      setNotice('Ticket type deleted.')
      await selectManagedEvent(managedEvent)
    } catch (requestError) {
      setError(requestError instanceof Error ? requestError.message : 'Unable to delete ticket type.')
    } finally {
      setLoading(false)
    }
  }

  function signOut() {
    localStorage.removeItem('booking-token')
    localStorage.removeItem('booking-user')
    setToken('')
    setCurrentUser(null)
    setEvents([])
    setBookings([])
    setSelectedEvent(null)
    setProfileMenuOpen(false)
    setShowAuth(true)
    setNotice('You have been signed out.')
  }

  return (
    <main className="app-shell">
      <header className="topbar">
        <button className="brand" type="button" onClick={() => setActiveView('discover')} aria-label="Go to Evently home"><span className="brand-mark">E</span><span>Evently</span></button>
        <nav aria-label="Primary navigation"><button className={activeView === 'discover' ? 'nav-link active' : 'nav-link'} type="button" onClick={() => setActiveView('discover')}>Discover</button><button className={activeView === 'bookings' ? 'nav-link active' : 'nav-link'} type="button" onClick={() => setActiveView('bookings')}>My bookings</button>{currentUser?.role === 'ORGANIZER' && <button className={activeView === 'manage' ? 'nav-link active' : 'nav-link'} type="button" onClick={() => setActiveView('manage')}>Manage events</button>}</nav>
        <div className="topbar-actions">
          {currentUser?.role === 'ORGANIZER' && <button className="button button-secondary" type="button" onClick={() => setShowCreateEvent(true)}>Create event</button>}
          {currentUser ? <div className="profile-menu"><button className="profile-button" type="button" onClick={() => setProfileMenuOpen((open) => !open)} aria-expanded={profileMenuOpen} aria-haspopup="menu" title="Open account menu"><span className="avatar">{currentUser.prenom.slice(0, 1)}{currentUser.nom.slice(0, 1)}</span><span className="profile-copy"><strong>{currentUser.prenom} {currentUser.nom}</strong><small>{currentUser.role === 'ORGANIZER' ? 'Organizer' : 'Attendee'}</small></span><span className="profile-caret" aria-hidden="true">v</span></button>{profileMenuOpen && <div className="profile-dropdown" role="menu"><div className="profile-dropdown-user"><strong>{currentUser.prenom} {currentUser.nom}</strong><span>{currentUser.email}</span></div><button type="button" role="menuitem" onClick={signOut}>Log out</button></div>}</div> : <button className="button button-primary" type="button" onClick={() => setShowAuth(true)}>Sign in</button>}
        </div>
      </header>
      {(notice || error) && <div className={`message ${error ? 'message-error' : 'message-success'}`} role="status"><span>{error || notice}</span><button type="button" onClick={() => { setNotice(''); setError('') }} aria-label="Dismiss message">x</button></div>}
      {activeView === 'discover' ? <>
        <section className="intro"><div><p className="eyebrow">EVENT BOOKING</p><h1>Find something worth showing up for.</h1><p className="intro-copy">A focused place to explore upcoming events and reserve your seat in a few calm clicks.</p></div><div className="stats" aria-label="Event statistics"><span><strong>{events.length}</strong> events</span><span><strong>{events.filter((event) => event.status === 'PUBLISHED').length}</strong> open now</span></div></section>
        <section className="toolbar" aria-label="Event filters"><label className="search-field"><span>Search events</span><input value={search} onChange={(event) => setSearch(event.target.value)} placeholder="Title, venue, or keyword" /></label><div className="filter-group" aria-label="Filter by status">{(['PUBLISHED', 'ALL', 'DRAFT', 'SOLD_OUT'] as const).map((status) => <button key={status} type="button" onClick={() => setSelectedStatus(status)} className={selectedStatus === status ? 'filter active' : 'filter'}>{status === 'ALL' ? 'All events' : status.replace('_', ' ').toLowerCase()}</button>)}</div></section>
        <section className="content-area"><div className="event-list"><div className="section-heading"><h2>Upcoming events</h2><span>{loading ? 'Refreshing...' : `${filteredEvents.length} shown`}</span></div>{loading && events.length === 0 ? <div className="empty-state">Loading the event calendar...</div> : filteredEvents.length === 0 ? <div className="empty-state">No events match these filters yet.</div> : filteredEvents.map((event) => <article className="event-row" key={event.id}><div className="date-badge"><strong>{new Date(event.date).toLocaleDateString('en', { month: 'short' })}</strong><span>{new Date(event.date).getDate()}</span></div><div className="event-summary"><div className="event-title-line"><h3>{event.title}</h3><span className={`status ${event.status.toLowerCase()}`}>{event.status.replace('_', ' ')}</span></div><p>{formatDate(event.date)} · {event.venue}</p><p className="description">{event.description || 'Details will be shared by the organizer.'}</p></div><button className="button button-secondary" type="button" onClick={() => void openEvent(event)}>View event</button></article>)}</div><aside className="side-note"><p className="eyebrow">YOUR PLACE</p><h2>Reservations that stay simple.</h2><p>Choose a ticket type, set your quantity, and we will keep your booking history close at hand.</p>{!currentUser && <button className="button button-primary" type="button" onClick={() => setShowAuth(true)}>Create an account</button>}</aside></section>
      </> : activeView === 'bookings' ? <section className="bookings-view"><p className="eyebrow">ACCOUNT</p><h1>My bookings</h1><p className="intro-copy">Your reserved tickets, all in one place.</p><div className="booking-table">{!currentUser ? <div className="empty-state">Sign in to see your booking history.</div> : bookings.length === 0 ? <div className="empty-state">You have not booked an event yet. Start exploring to find your next plan.</div> : bookings.map((booking) => <div className="booking-row" key={booking.id}><div><strong>{booking.ticketTypeName}</strong><span>Booked {formatDate(booking.bookedAt)}</span></div><span>{booking.quantity} ticket{booking.quantity > 1 ? 's' : ''}</span><span className={`status ${booking.status.toLowerCase()}`}>{booking.status}</span></div>)}</div></section> : <OrganizerWorkspace events={events.filter((event) => event.organizerId === currentUser?.id)} selectedEvent={managedEvent} tickets={managedTickets} onSelect={selectManagedEvent} onCreate={() => setShowCreateEvent(true)} onEditEvent={setEditingEvent} onDeleteEvent={deleteEvent} onCreateTicket={() => { setEditingTicket(null); setShowTicketForm(true) }} onEditTicket={(ticket) => { setEditingTicket(ticket); setShowTicketForm(true) }} onDeleteTicket={deleteTicket} loading={loading} />}
      {showAuth && <AuthDialog onClose={() => setShowAuth(false)} onSubmit={handleAuth} loading={loading} />}
      {selectedEvent && <EventDialog event={selectedEvent} tickets={tickets} onClose={() => setSelectedEvent(null)} onBook={createBooking} loading={loading} signedIn={Boolean(currentUser)} />}
      {showCreateEvent && <EventFormDialog onClose={() => setShowCreateEvent(false)} onSubmit={createEvent} loading={loading} />}
      {editingEvent && <EventFormDialog event={editingEvent} onClose={() => setEditingEvent(null)} onSubmit={updateEvent} loading={loading} />}
      {showTicketForm && <TicketFormDialog ticket={editingTicket} onClose={() => { setEditingTicket(null); setShowTicketForm(false) }} onSubmit={saveTicket} loading={loading} />}
    </main>
  )
}

function AuthDialog({ onClose, onSubmit, loading }: { onClose: () => void; onSubmit: (event: FormEvent<HTMLFormElement>) => void; loading: boolean }) {
  const [mode, setMode] = useState<'login' | 'register'>('login')
  return <div className="dialog-backdrop" role="presentation"><section className="dialog auth-dialog" role="dialog" aria-modal="true" aria-labelledby="auth-title"><button className="close-button" type="button" onClick={onClose} aria-label="Close">x</button><p className="eyebrow">EVENTLY ACCOUNT</p><h2 id="auth-title">{mode === 'login' ? 'Welcome back.' : 'Make room for better plans.'}</h2><p>{mode === 'login' ? 'Sign in to reserve tickets and see your bookings.' : 'Create your account to start discovering events.'}</p><form onSubmit={onSubmit}><input type="hidden" name="mode" value={mode} />{mode === 'register' && <div className="form-grid"><label>First name<input required name="prenom" autoComplete="given-name" /></label><label>Last name<input required name="nom" autoComplete="family-name" /></label></div>}<label>Email<input required type="email" name="email" autoComplete="email" /></label><label>Password<input required type="password" name="password" minLength={6} autoComplete={mode === 'login' ? 'current-password' : 'new-password'} /></label>{mode === 'register' && <label>Account type<select name="role" defaultValue="ATTENDEE"><option value="ATTENDEE">Attendee</option><option value="ORGANIZER">Organizer</option></select></label>}<button className="button button-primary submit-button" disabled={loading}>{loading ? 'Please wait...' : mode === 'login' ? 'Sign in' : 'Create account'}</button></form><button className="text-button" type="button" onClick={() => setMode(mode === 'login' ? 'register' : 'login')}>{mode === 'login' ? 'New here? Create an account' : 'Already have an account? Sign in'}</button></section></div>
}

function EventDialog({ event, tickets, onClose, onBook, loading, signedIn }: { event: BookingEvent; tickets: TicketType[]; onClose: () => void; onBook: (ticket: TicketType, quantity: number) => void; loading: boolean; signedIn: boolean }) {
  const [selectedTicketId, setSelectedTicketId] = useState<number | null>(null)
  const [quantity, setQuantity] = useState(1)
  const selectedTicket = tickets.find((ticket) => ticket.id === selectedTicketId)
  return <div className="dialog-backdrop" role="presentation"><section className="dialog event-dialog" role="dialog" aria-modal="true" aria-labelledby="event-title"><button className="close-button" type="button" onClick={onClose} aria-label="Close">x</button><div className="event-dialog-header"><p className="eyebrow">{event.status.replace('_', ' ')}</p><h2 id="event-title">{event.title}</h2><p>{formatDate(event.date)}<br />{event.venue}</p></div><p className="event-detail-description">{event.description || 'The organizer has not shared a full description yet.'}</p><div className="ticket-options"><h3>Choose tickets</h3>{tickets.length === 0 ? <p className="muted">No ticket types have been added to this event yet.</p> : tickets.map((ticket) => <label className={selectedTicketId === ticket.id ? 'ticket-option selected' : 'ticket-option'} key={ticket.id}><input type="radio" name="ticket" checked={selectedTicketId === ticket.id} onChange={() => { setSelectedTicketId(ticket.id); setQuantity(1) }} disabled={ticket.quantityAvailable === 0} /><span><strong>{ticket.name}</strong><small>{ticket.quantityAvailable} remaining</small></span><strong>{moneyFormatter.format(ticket.price)}</strong></label>)}</div>{selectedTicket && <div className="booking-action"><label>Quantity<input type="number" min="1" max={selectedTicket.quantityAvailable} value={quantity} onChange={(input) => setQuantity(Math.min(selectedTicket.quantityAvailable, Math.max(1, Number(input.target.value))))} /></label><div><strong>{moneyFormatter.format(selectedTicket.price * quantity)}</strong><button className="button button-primary" type="button" disabled={loading || !signedIn} onClick={() => onBook(selectedTicket, quantity)}>{!signedIn ? 'Sign in to book' : loading ? 'Reserving...' : 'Reserve tickets'}</button></div></div>}</section></div>
}

function OrganizerWorkspace({ events, selectedEvent, tickets, onSelect, onCreate, onEditEvent, onDeleteEvent, onCreateTicket, onEditTicket, onDeleteTicket, loading }: { events: BookingEvent[]; selectedEvent: BookingEvent | null; tickets: TicketType[]; onSelect: (event: BookingEvent) => void; onCreate: () => void; onEditEvent: (event: BookingEvent) => void; onDeleteEvent: (event: BookingEvent) => void; onCreateTicket: () => void; onEditTicket: (ticket: TicketType) => void; onDeleteTicket: (ticket: TicketType) => void; loading: boolean }) {
  return <section className="manager-view"><div className="manager-heading"><div><p className="eyebrow">ORGANIZER WORKSPACE</p><h1>Manage your events</h1><p className="intro-copy">Choose an event to update details, manage ticket types, and keep inventory accurate.</p></div><button className="button button-primary" type="button" onClick={onCreate}>Create event</button></div><div className="manager-layout"><div className="manager-list"><h2>Your events</h2>{events.length === 0 ? <div className="empty-state">No events yet. Create your first one to begin.</div> : events.map((event) => <button type="button" className={selectedEvent?.id === event.id ? 'managed-event selected' : 'managed-event'} key={event.id} onClick={() => void onSelect(event)}><span><strong>{event.title}</strong><small>{formatDate(event.date)} · {event.venue}</small></span><span className={`status ${event.status.toLowerCase()}`}>{event.status}</span></button>)}</div><div className="manager-detail">{!selectedEvent ? <div className="empty-state">Choose one of your events to manage it.</div> : <><div className="manager-event-header"><div><p className="eyebrow">{selectedEvent.status}</p><h2>{selectedEvent.title}</h2><p>{selectedEvent.venue} · {formatDate(selectedEvent.date)}</p></div><div className="manager-actions"><button className="button button-secondary" type="button" onClick={() => onEditEvent(selectedEvent)}>Edit event</button><button className="danger-button" type="button" disabled={loading} onClick={() => onDeleteEvent(selectedEvent)}>Delete</button></div></div><div className="ticket-manager"><div className="section-heading"><h2>Ticket types</h2><button className="button button-primary" type="button" onClick={onCreateTicket}>Add ticket type</button></div>{tickets.length === 0 ? <div className="empty-state">This event has no ticket types yet.</div> : tickets.map((ticket) => <div className="ticket-manager-row" key={ticket.id}><div><strong>{ticket.name}</strong><span>{moneyFormatter.format(ticket.price)} · {ticket.quantityAvailable} available</span></div><div><button className="text-button" type="button" onClick={() => onEditTicket(ticket)}>Edit</button><button className="danger-button" type="button" disabled={loading} onClick={() => onDeleteTicket(ticket)}>Delete</button></div></div>)}</div></>}</div></div></section>
}

function EventFormDialog({ event, onClose, onSubmit, loading }: { event?: BookingEvent; onClose: () => void; onSubmit: (event: FormEvent<HTMLFormElement>) => void; loading: boolean }) {
  const minimumDate = new Date(Date.now() + 60_000).toISOString().slice(0, 16)
  const localDate = event?.date ? event.date.slice(0, 16) : undefined
  return <div className="dialog-backdrop" role="presentation"><section className="dialog create-dialog" role="dialog" aria-modal="true" aria-labelledby="event-form-title"><button className="close-button" type="button" onClick={onClose} aria-label="Close">x</button><p className="eyebrow">ORGANIZER</p><h2 id="event-form-title">{event ? 'Edit event' : 'Publish a new event'}</h2><form onSubmit={onSubmit}><label>Event title<input required name="title" maxLength={128} defaultValue={event?.title} /></label><label>Venue<input required name="venue" maxLength={255} defaultValue={event?.venue} /></label><div className="form-grid"><label>Date and time<input required type="datetime-local" min={minimumDate} name="date" defaultValue={localDate} /></label><label>Capacity<input required type="number" min="1" name="capacity" defaultValue={event?.capacity} /></label></div><label>Status<select name="status" defaultValue={event?.status ?? 'PUBLISHED'}><option value="DRAFT">Draft</option><option value="PUBLISHED">Published</option><option value="CANCELLED">Cancelled</option></select></label><label>Description<textarea name="description" maxLength={2000} rows={4} defaultValue={event?.description} /></label><button className="button button-primary submit-button" disabled={loading}>{loading ? 'Saving...' : event ? 'Save changes' : 'Publish event'}</button></form></section></div>
}

function TicketFormDialog({ ticket, onClose, onSubmit, loading }: { ticket: TicketType | null; onClose: () => void; onSubmit: (event: FormEvent<HTMLFormElement>) => void; loading: boolean }) {
  return <div className="dialog-backdrop" role="presentation"><section className="dialog create-dialog" role="dialog" aria-modal="true" aria-labelledby="ticket-form-title"><button className="close-button" type="button" onClick={onClose} aria-label="Close">x</button><p className="eyebrow">TICKET INVENTORY</p><h2 id="ticket-form-title">{ticket ? 'Edit ticket type' : 'Add ticket type'}</h2><form onSubmit={onSubmit}><label>Ticket name<input required name="name" maxLength={64} defaultValue={ticket?.name} /></label><div className="form-grid"><label>Price<input required type="number" min="0" step="0.01" name="price" defaultValue={ticket?.price} /></label><label>Available quantity<input required type="number" min="0" name="quantityAvailable" defaultValue={ticket?.quantityAvailable} /></label></div><button className="button button-primary submit-button" disabled={loading}>{loading ? 'Saving...' : ticket ? 'Save ticket type' : 'Add ticket type'}</button></form></section></div>
}

export default App
