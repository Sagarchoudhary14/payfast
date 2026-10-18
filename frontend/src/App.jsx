import { useEffect, useMemo, useState } from 'react';
import { api } from './api.js';

const money = (amount, currency) => new Intl.NumberFormat('en-IN', {
  style: 'currency', currency: currency || 'INR', maximumFractionDigits: 2
}).format(amount);

export default function App() {
  const [token, setToken] = useState(() => sessionStorage.getItem('token') || '');
  const [user, setUser] = useState(() => sessionStorage.getItem('user') || '');
  const [role, setRole] = useState(() => sessionStorage.getItem('role') || '');
  const [email, setEmail] = useState('customer@example.com');
  const [password, setPassword] = useState('Customer123!');
  const [recipient, setRecipient] = useState('merchant-123');
  const [amount, setAmount] = useState('1250.00');
  const [currency, setCurrency] = useState('INR');
  const [description, setDescription] = useState('Order payment');
  const [transactions, setTransactions] = useState([]);
  const [busy, setBusy] = useState(false);
  const [notice, setNotice] = useState('');
  const [error, setError] = useState('');

  const authorizedCount = useMemo(() => transactions.filter(t => t.status === 'AUTHORIZED').length, [transactions]);
  const totalAuthorized = useMemo(() => transactions.filter(t => t.status === 'AUTHORIZED')
    .reduce((sum, t) => sum + Number(t.amount), 0), [transactions]);

  async function refresh(nextToken = token) {
    if (!nextToken) return;
    try { setTransactions(await api('/api/payments', { token: nextToken })); }
    catch (e) { setError(e.message); }
  }

  useEffect(() => { if (token) refresh(token); }, [token]);

  async function login(event) {
    event.preventDefault(); setBusy(true); setError(''); setNotice('');
    try {
      const result = await api('/api/auth/login', {
        method: 'POST', body: JSON.stringify({ email, password })
      });
      setToken(result.token); setUser(result.email); setRole(result.role);
      sessionStorage.setItem('token', result.token);
      sessionStorage.setItem('user', result.email);
      sessionStorage.setItem('role', result.role);
      setNotice('Signed in successfully.');
    } catch (e) { setError(e.message); }
    finally { setBusy(false); }
  }

  function logout() {
    setToken(''); setUser(''); setRole(''); setTransactions([]);
    sessionStorage.clear(); setNotice('You have signed out.');
  }

  async function submitPayment(event) {
    event.preventDefault(); setBusy(true); setError(''); setNotice('');
    const idempotencyKey = crypto.randomUUID();
    try {
      const result = await api('/api/payments', {
        method: 'POST', token,
        headers: { 'Idempotency-Key': idempotencyKey },
        body: JSON.stringify({ recipient, amount: Number(amount), currency, description })
      });
      setNotice(`Payment ${result.status.toLowerCase()} · transaction #${result.id}${result.fraudReason ? ` · ${result.fraudReason}` : ''}`);
      await refresh();
    } catch (e) { setError(e.message); }
    finally { setBusy(false); }
  }

  return <main className="shell">
    <nav className="topbar">
      <a className="brand" href="#"><span className="brand-mark">P</span><span>payflow<span className="brand-dot">.</span></span></a>
      <div className="nav-right">
        <span className="live-dot" /> <span className="muted">SANDBOX ENVIRONMENT</span>
        {token && <button className="button button-quiet" onClick={logout}>Sign out</button>}
      </div>
    </nav>

    <section className="hero">
      <div>
        <div className="eyebrow"><span className="eyebrow-line" /> PAYMENT OPERATIONS</div>
        <h1>Move money.<br /><span>With confidence.</span></h1>
        <p className="hero-copy">A secure transaction console with authorization controls, idempotent requests, and real-time rule-based fraud screening.</p>
      </div>
      <div className="hero-visual" aria-hidden="true">
        <div className="orb orb-one" /><div className="orb orb-two" />
        <div className="visual-card"><div className="visual-card-top"><span>PAYMENT FLOW</span><span className="mini-status">● ACTIVE</span></div>
          <div className="flow-node"><span className="node-icon">↗</span><div><b>Authorization</b><small>Request validated</small></div><span className="check">✓</span></div>
          <div className="flow-connector" />
          <div className="flow-node"><span className="node-icon shield">◇</span><div><b>Fraud screening</b><small>Rules evaluated</small></div><span className="check">✓</span></div>
          <div className="flow-connector" />
          <div className="flow-node"><span className="node-icon database">▤</span><div><b>Transaction ledger</b><small>Idempotency protected</small></div><span className="check">✓</span></div>
        </div>
      </div>
    </section>

    {notice && <div className="notice success"><span>✓</span>{notice}<button onClick={() => setNotice('')}>×</button></div>}
    {error && <div className="notice failure"><span>!</span>{error}<button onClick={() => setError('')}>×</button></div>}

    {!token ? <section className="login-layout">
      <div className="section-heading"><div className="eyebrow">GET STARTED</div><h2>Welcome back.</h2><p>Sign in to access your transaction workspace.</p></div>
      <form className="panel login-panel" onSubmit={login}>
        <div className="panel-title"><div><h3>Sign in</h3><p>Use your sandbox account to continue.</p></div><span className="lock-icon">⌑</span></div>
        <label>Email address</label><input type="email" value={email} onChange={e => setEmail(e.target.value)} required />
        <label>Password</label><input type="password" value={password} onChange={e => setPassword(e.target.value)} required />
        <button className="button button-primary full" disabled={busy}>{busy ? 'Signing in…' : 'Continue'} <span>→</span></button>
        <div className="demo-hint"><b>Demo credentials</b><span>customer@example.com / Customer123!</span><span>admin@example.com / Admin123!</span></div>
      </form>
    </section> : <>
      <div className="workspace-head"><div><div className="eyebrow">YOUR WORKSPACE</div><h2>Transaction overview</h2><p>Signed in as {user} <span className="role-pill">{role}</span></p></div><button className="button button-outline" onClick={() => refresh()}>↻ Refresh data</button></div>
      <section className="stats-grid">
        <article className="stat-card"><div className="stat-label">TOTAL TRANSACTIONS <span>▤</span></div><div className="stat-value">{transactions.length}</div><div className="stat-foot">Recorded in your ledger</div></article>
        <article className="stat-card"><div className="stat-label">AUTHORIZED <span className="green-icon">✓</span></div><div className="stat-value">{authorizedCount}</div><div className="stat-foot"><span className="green-text">●</span> Passed fraud checks</div></article>
        <article className="stat-card"><div className="stat-label">AUTHORIZED VOLUME <span>₹</span></div><div className="stat-value stat-money">{money(totalAuthorized, 'INR')}</div><div className="stat-foot">Sum of authorized amounts</div></article>
      </section>
      <section className="content-grid">
        <form className="panel payment-panel" onSubmit={submitPayment}>
          <div className="panel-title"><div><h3>New payment</h3><p>Submit a payment authorization request.</p></div><span className="panel-number">01</span></div>
          <label>Recipient / merchant ID</label><input value={recipient} onChange={e => setRecipient(e.target.value)} maxLength="120" required />
          <div className="form-row"><div className="field-grow"><label>Amount</label><input type="number" min="0.01" step="0.01" value={amount} onChange={e => setAmount(e.target.value)} required /></div>
            <div className="currency-field"><label>Currency</label><select value={currency} onChange={e => setCurrency(e.target.value)}><option>INR</option><option>USD</option><option>EUR</option><option>GBP</option></select></div></div>
          <label>Description <span className="optional">OPTIONAL</span></label><input value={description} onChange={e => setDescription(e.target.value)} maxLength="500" />
          <div className="security-note"><span>◇</span><p><b>Protected request</b><br />A unique idempotency key is generated for each submission.</p></div>
          <button className="button button-primary full" disabled={busy}>{busy ? 'Processing…' : 'Authorize payment'} <span>→</span></button>
        </form>
        <section className="panel transactions-panel">
          <div className="panel-title"><div><h3>Recent transactions</h3><p>Your latest authorization activity.</p></div><span className="panel-number">02</span></div>
          {transactions.length === 0 ? <div className="empty-state"><div className="empty-icon">↗</div><b>No transactions yet</b><p>Submit a payment to see it appear here.</p></div> :
            <div className="table-wrap"><table><thead><tr><th>TRANSACTION</th><th>AMOUNT</th><th>STATUS</th></tr></thead><tbody>
              {transactions.map(t => <tr key={t.id}><td><b>#{t.id} · {t.recipient}</b><small>{new Date(t.createdAt).toLocaleString()}</small></td><td className="amount-cell">{money(t.amount, t.currency)}</td><td><span className={`status ${t.status.toLowerCase()}`}>{t.status}</span>{t.fraudReason && <small className="fraud-reason">{t.fraudReason}</small>}</td></tr>)}
            </tbody></table></div>}
        </section>
      </section>
    </>}
    <footer><span>PAYFLOW <b>·</b> TRANSACTION CONSOLE</span><span>Built for safe-by-design payment flows <span className="footer-heart">✳</span></span></footer>
  </main>;
}
