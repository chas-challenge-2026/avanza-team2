# native/ — v2 C/C++-moduler

Denna katalog innehåller projektets prestandakritiska nativmoduler.
Modulerna anropas från Java via JNA (Java Native Access).

## Moduler

### backtest/ — Backtestmotor
- **Språk:** C++17
- **Syfte:** Simulering av investeringsstrategier mot 5 år historisk data (upp till 500 instrument)
- **JNA-brygga:** `se.comerit.avanza.native.BacktestBridge`
- **Anledning till native:** JVM-GC orsakar oförutsägbara pauser vid stora tidsserieoperationer
- **Dokumentation:** [backtest/README.md](backtest/README.md)

### risk/ — Rullande riskmått
- **Språk:** C med BLAS-optimering
- **Syfte:** Beräknar volatilitet, Sharpe-kvot och max drawdown över stora tidsserier
- **Anledning till native:** Vektoriserade SIMD-operationer för 10x–50x snabbare beräkning än pure Java
- **Dokumentation:** [risk/README.md](risk/README.md)

### fx/ — FX-pipeline
- **Språk:** C++
- **Syfte:** Historisk FX-lookup mot ECB-data + realtidshämtning
- **Anledning till native:** Låg latens, undviker JVM-overhead för nätverks-I/O i hot path
- **Dokumentation:** [fx/README.md](fx/README.md)

## Bygg

```bash
git submodule update --init --recursive
make install   # dependencies
make all       # build all modules
```

Rensa byggfiler:
```bash
make clean
```

Bygg enskild modul:

```bash
make risk
```

Run a specific target within a module (see each module's README for available targets):
```bash
make risk/clean   # make <module>/<target>
```

## Status
Alla tre moduler fungerar och uppnår sitt syfte.
