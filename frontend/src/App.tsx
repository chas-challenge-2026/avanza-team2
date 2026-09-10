import { useState } from 'react'
import { Title } from './Components/Title/Title.tsx'
import { DropdownBtn } from './Components/DropdownBtn/DropdownBtn.tsx'
import { StatCard } from './Components/StatCard/StatCard.tsx'
import { SparklineChart } from './Components/SparklineChart/SparklineChart.tsx'

const sampleTrend = [10, 12, 11, 14, 13, 16, 15, 18, 17, 20, 19, 23]


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

      <div style={{ display: 'flex', flexDirection: 'column', gap: 16, maxWidth: 400, marginTop: 24 }}>
        <StatCard
          tone="accent"
          title="Totalt värde"
          value="264 284,7 SEK"
          delta={{ label: '1,23% idag', positive: true }}
          chart={<SparklineChart data={sampleTrend} />}
        />
         <StatCard
          tone="accent"
          title="Totalt värde"
          value="-264 900 SEK"
          delta={{ label: '1,23% idag', positive: false }}
          chart={<SparklineChart data={sampleTrend} />}
        />
        </div>
              <div style={{ display: 'flex', flexDirection: 'column', gap: 16, maxWidth: 280, marginTop: 24 }}>

       
        <StatCard title="Tillgängligt för köp" value="38 620,0 SEK" />
        <StatCard title="Totalt investerat" value="225 664,7 SEK" />
      </div>

    </div>



  )
}

export default App
