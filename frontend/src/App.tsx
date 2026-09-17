import { Navbar } from './Components/Navbar/Navbar.tsx'
import { TopBar } from './Components/TopBar/TopBar.tsx'
import { Footer } from './Components/Footer/Footer.tsx'
import { Routes, Route, useLocation, useNavigate } from 'react-router-dom'
import { Overview } from './Pages/Overview/Overview.tsx'
import { Analyses } from './Pages/Analyses/Analyses.tsx'
import { Holdings } from './Pages/Holdings/Holdings.tsx'
import { Notifications } from './Pages/Notifications/Notifications.tsx'
import { Reports } from './Pages/Reports/Reports.tsx'
import { Settings } from './Pages/Settings/Settings.tsx'
import { Login } from './Pages/Login/Login.tsx'
import { ForgotPassword } from './Pages/ForgotPassword/ForgotPassword.tsx'
import { ResetPassword } from './Pages/ResetPassword/ResetPassword.tsx'
import { ProtectedRoute } from './Components/ProtectedRoute/ProtectedRoute.tsx'
import { useAuth } from './context/useAuth';

const LOGIN_PATH = '/Loggain'
const PUBLIC_PATHS = [LOGIN_PATH, '/glomt-losenord', '/aterstall-losenord']

function App() {
  const location = useLocation()
  const navigate = useNavigate()
  const auth = useAuth()

  const handleLogin = () => {
    auth.login()
    navigate('/')
  }

  const routes = [
    { path: '/', title: 'Min Portfölj', element: <ProtectedRoute><Overview /></ProtectedRoute> },
    { path: '/innehav', title: 'Mina Innehav', element: <ProtectedRoute><Holdings /></ProtectedRoute> },
    { path: '/notiser', title: 'Notiser', element: <ProtectedRoute><Notifications /></ProtectedRoute> },
    { path: '/analyser', title: 'Analyser', element: <ProtectedRoute><Analyses /></ProtectedRoute> },
    { path: '/rapporter', title: 'Rapporter', element: <ProtectedRoute><Reports /></ProtectedRoute> },
    { path: '/installningar', title: 'Inställningar', element: <ProtectedRoute><Settings /></ProtectedRoute> },
    { path: LOGIN_PATH, title: 'Logga In', element: <Login onLogin={handleLogin} /> },
    { path: '/glomt-losenord', title: 'Glömt lösenord', element: <ForgotPassword /> },
    { path: '/aterstall-losenord/:token', title: 'Återställ lösenord', element: <ResetPassword /> },
  ]

  const currentTitle = routes.find((route) => route.path === location.pathname)?.title ?? 'Min Portfölj'
  const isPublicPage = PUBLIC_PATHS.some((path) => location.pathname.startsWith(path))

  if (isPublicPage) {
    return (
      <Routes>
        {routes.map((route) => (
          <Route key={route.path} path={route.path} element={route.element} />
        ))}
      </Routes>
    )
  }

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
