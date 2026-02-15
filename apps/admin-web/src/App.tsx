import './App.css'

type Role = 'ADMIN' | 'BROKER'

type Tile = {
  title: string
  description: string
}

const panels: Record<Role, Tile[]> = {
  ADMIN: [
    {
      title: 'Sirket Onay Kuyrugu',
      description: 'Bekleyen sirket kayitlarini onayla veya reddet.',
    },
    {
      title: 'Kategori Semalari',
      description: 'Dinamik kategori alanlarini yonet.',
    },
    {
      title: 'Audit Olaylari',
      description: 'Kritik islemleri izle ve raporla.',
    },
  ],
  BROKER: [
    {
      title: 'Aktif RFQ Listesi',
      description: 'Muzakere gerektiren talepleri incele.',
    },
    {
      title: 'Mudahale Is Akisi',
      description: 'Broker notu ve yonlendirme adimlarini kaydet.',
    },
    {
      title: 'Eslestirme Onerileri',
      description: 'Alici ve satici uygunluk skorlarini goruntule.',
    },
  ],
}

function App() {
  const role: Role = 'ADMIN'

  return (
    <main className="page">
      <header>
        <p className="eyebrow">trade-bridge admin</p>
        <h1>Yonetim Paneli</h1>
        <p className="subtitle">Rol: {role}</p>
      </header>

      <section className="grid" aria-label="role-panels">
        {panels[role].map((item) => (
          <article key={item.title} className="card">
            <h2>{item.title}</h2>
            <p>{item.description}</p>
          </article>
        ))}
      </section>
    </main>
  )
}

export default App
