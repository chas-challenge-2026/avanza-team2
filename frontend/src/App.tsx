import { useState } from 'react'
import { Title } from './Components/Title/Title.tsx'
import { DropdownBtn } from './Components/DropdownBtn/DropdownBtn.tsx'
import { StatCard } from './Components/StatCard/StatCard.tsx'
import { SparklineChart } from './Components/SparklineChart/SparklineChart.tsx'
import { Navbar } from './Components/Navbar/Navbar.tsx'
import { TopBar } from './Components/TopBar/TopBar.tsx'
import { Footer } from './Components/Footer/Footer.tsx'
import { Routes, Route, useLocation } from 'react-router-dom'
import { Overview } from './Pages/Overview/Overview.tsx'
import { Analyses } from './Pages/Analyses/Analyses.tsx'
import { Holdings } from './Pages/Holdings/Holdings.tsx'
import { Notifications } from './Pages/Notifications/Notifications.tsx'
import { Reports } from './Pages/Reports/Reports.tsx'
import { Settings } from './Pages/Settings/Settings.tsx'

const sampleTrend = [10, 12, 11, 14, 13, 16, 15, 18, 17, 20, 19, 23]

const routes = [
  { path: '/', title: 'Min Portfölj', element: <Overview /> },
  { path: '/innehav', title: 'Mina Innehav', element: <Holdings /> },
  { path: '/notiser', title: 'Notiser', element: <Notifications /> },
  { path: '/analyser', title: 'Analyser', element: <Analyses /> },
  { path: '/rapporter', title: 'Rapporter', element: <Reports /> },
  { path: '/installningar', title: 'Inställningar', element: <Settings /> },
]

function App() {
  const [account, setAccount] = useState<string | undefined>()
  const location = useLocation()
  const currentTitle = routes.find((route) => route.path === location.pathname)?.title ?? 'Min Portfölj'

  return (


    <div className="flex h-screen flex-col">
      <TopBar title={currentTitle} />

      <div className="flex flex-1 overflow-hidden">
        <Navbar />

        <div className="flex-1 overflow-y-auto">
          <Routes>
            {routes.map((route) => (
              <Route key={route.path} path={route.path} element={route.element} />
            ))}
          </Routes>
        </div>
      </div>

      <Footer />
      {/* <DropdownBtn placeholder="Alla konton"
        options={[
          { label: 'Anna ISK', value: 'isk' },
          { label: 'Anna KF', value: 'kf' },
          { label: 'Anna Depå', value: 'depa' },
        ]}
        value={account}
        onChange={setAccount}
        /> */}

      {/* <div style={{ display: 'flex', flexDirection: 'column', gap: 16, maxWidth: 400, marginTop: 24 }}>
         <StatCard
          tone="accent"
          title="Totalt värde"
          value="-264 900 SEK"
          delta={{ label: '1,23% idag', positive: false }}
          chart={<SparklineChart data={sampleTrend} />}
        />
        </div> */}

    </div>



  )
}

export default App
