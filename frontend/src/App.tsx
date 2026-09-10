import { useState } from 'react'
import { Title } from './Components/Title/Title.tsx'
import { DropdownBtn } from './Components/DropdownBtn/DropdownBtn.tsx'


function App() {
  const [account, setAccount] = useState<string | undefined>()

  return (


    <div>
      <Title variant="secondary">Översikt</Title>
      <DropdownBtn placeholder="Alla konton"
        options={[
          { label: 'Anna ISK', value: 'isk' },
          { label: 'Anna KF', value: 'kf' },
          { label: 'Anna Depå', value: 'depa' },
        ]}
        value={account}
        onChange={setAccount}
        />
    </div>



  )
}

export default App
