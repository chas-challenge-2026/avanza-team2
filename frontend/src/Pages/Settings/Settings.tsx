import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { SettingsSection } from "../../Components/SettingsSection/SettingsSection.tsx";
import { SettingsRow } from "../../Components/SettingsRow/SettingsRow.tsx";
import { Toggle } from "../../Components/Toggle/Toggle.tsx";
import { deviationAlert } from "./settingsSampleData.ts";
import {
  getTargetAllocations,
  updateTargetAllocations,
  getPortfolio,
  type TargetAllocation,
} from "../../Services/targetAllocationService";
import {
  areDriftNotificationsEnabled,
  setDriftNotificationsEnabled,
} from "../../utils/driftNotificationPreference.ts";
import { useAuth } from "../../context/useAuth.ts";

export interface AccountTargetItem {
  accountId: number;
  accountName: string;
  allocations: TargetAllocation[];
}

export const Settings = () => {
  const navigate = useNavigate();
  const { logout } = useAuth();
  const [loggingOut, setLoggingOut] = useState(false);

  const [deviationEnabled, setDeviationEnabled] = useState(
    areDriftNotificationsEnabled,
  );

  const [userAccounts, setUserAccounts] = useState<AccountTargetItem[]>([]);
  const [draftUserAccounts, setDraftUserAccounts] = useState<AccountTargetItem[]>([]);
  const [activeEditingAccountId, setActiveEditingAccountId] = useState<number | null>(null);

  const [loadingAllocations, setLoadingAllocations] = useState(true);
  const [savingAllocations, setSavingAllocations] = useState(false);
  const [allocationError, setAllocationError] = useState<string | null>(null);
  const [allocationSuccess, setAllocationSuccess] = useState<string | null>(null);

  // Hämta riktig data från backend
  useEffect(() => {
    let isMounted = true;

    const loadData = async () => {
      try {
        setLoadingAllocations(true);
        setAllocationError(null);

        // Hämtar riktig portfölj och riktiga mål från backend
        const [portfolioData, rawTargets] = await Promise.all([
          getPortfolio(),
          getTargetAllocations(),
        ]);

        if (isMounted) {
          // Konton utifrån backend-svaret - begränsar till Aktier / Fonder
          const realAccounts: AccountTargetItem[] = (portfolioData.accountSummary || []).map(
            (acc, index) => {
              // Filtrera så vi enbart har Aktier och Fonder 
              const relevantTargets = rawTargets.filter(
                (t) => t.accountType === "Aktier" || t.accountType === "Fonder"
              );

              return {
                accountId: acc.accountId || index + 1,
                accountName: acc.accountName 
                  ? `${acc.accountName} (${acc.accountType})` 
                  : acc.accountType || `Konto ${index + 1}`,
                // Använder riktiga mål från backend
                allocations: relevantTargets.length > 0 
                  ? relevantTargets.map(t => ({ ...t })) 
                  : [
                      { id: 1, accountType: "Aktier", targetPercentage: 50 },
                      { id: 2, accountType: "Fonder", targetPercentage: 50 },
                    ],
              };
            },
          );

          setUserAccounts(realAccounts);
          setDraftUserAccounts(realAccounts);
        }
      } catch {
        if (isMounted) {
          setAllocationError("Kunde inte hämta riktiga konton från servern.");
        }
      } finally {
        if (isMounted) {
          setLoadingAllocations(false);
        }
      }
    };

    void loadData();

    return () => {
      isMounted = false;
    };
  }, []);

  const activeAccountTotal = useMemo(() => {
    if (activeEditingAccountId === null) return 0;
    const account = draftUserAccounts.find((a) => a.accountId === activeEditingAccountId);
    if (!account) return 0;

    return account.allocations.reduce(
      (sum, item) => sum + item.targetPercentage,
      0,
    );
  }, [draftUserAccounts, activeEditingAccountId]);

  const toggleAccountEditor = (accountId: number) => {
    if (savingAllocations) return;

    if (activeEditingAccountId === accountId) {
      setActiveEditingAccountId(null);
      setAllocationError(null);
      return;
    }

    setDraftUserAccounts(JSON.parse(JSON.stringify(userAccounts)));
    setAllocationError(null);
    setAllocationSuccess(null);
    setActiveEditingAccountId(accountId);
  };

  const updateDraftPercentage = (accountId: number, id: number, value: string) => {
    const percentage = value.trim() === "" ? 0 : Number(value);

    if (!Number.isFinite(percentage) || percentage < 0 || percentage > 100) {
      return;
    }

    setDraftUserAccounts((current) =>
      current.map((acc) => {
        if (acc.accountId !== accountId) return acc;
        return {
          ...acc,
          allocations: acc.allocations.map((item) =>
            item.id === id ? { ...item, targetPercentage: percentage } : item,
          ),
        };
      }),
    );

    setAllocationError(null);
  };

// Spara ändringen för ett specifikt konto
const saveAccountAllocations = async (accountId: number) => {
  const targetAccount = draftUserAccounts.find((a) => a.accountId === accountId);

  // Kontrollera att kontot finns och att Aktier + Fonder summerar till exakt 100%
  if (!targetAccount || Math.round(activeAccountTotal) !== 100) {
    setAllocationError(
      "Målfördelningen för kontot måste summera till exakt 100 % (Aktier + Fonder).",
    );
    return;
  }

  try {
    setSavingAllocations(true);
    setAllocationError(null);
    setAllocationSuccess(null);

    // Skicka ändringen till backend
    const payload = targetAccount.allocations.map(({ accountType, targetPercentage }) => ({
      accountType,
      targetPercentage,
    }));

    await updateTargetAllocations(payload);

    setUserAccounts((current) =>
      current.map((acc) =>
        acc.accountId === accountId
          ? { ...acc, allocations: [...targetAccount.allocations] }
          : acc,
      ),
    );

    setActiveEditingAccountId(null);
    setAllocationSuccess(
      `Ny målfördelning har sparats för ${targetAccount.accountName}!`,
    );
  } catch {
    // Fallback bara för att testa i frontend
    setUserAccounts((current) =>
      current.map((acc) =>
        acc.accountId === accountId
          ? { ...acc, allocations: [...targetAccount.allocations] }
          : acc,
      ),
    );

    setActiveEditingAccountId(null);
    setAllocationSuccess(
      `Målfördelningen uppdaterades lokalt för ${targetAccount.accountName}.`,
    );
  } finally {
    setSavingAllocations(false);
  }
};

  const handleLogout = async () => {
    setLoggingOut(true);
    try {
      await logout();
    } finally {
      navigate("/Loggain");
    }
  };

  return (
    <div className="p-6">
      <div className="mx-auto mt-6 flex max-w-2xl flex-col gap-6">
        <SettingsSection title="Allmänt">
          <SettingsRow
            label="Standardvaluta"
            subtext="Valutakonvertering är inte tillgänglig ännu."
            control={
              <span className="rounded-md bg-neutral-100 px-3 py-1.5 text-sm text-neutral-500">
                SEK · Ej tillgänglig
              </span>
            }
          />
        </SettingsSection>

        <SettingsSection title="Portföljmål per konto (Aktier & Fonder)">
          {loadingAllocations ? (
            <div className="p-4 text-sm text-neutral-500">Hämtar riktiga konton från backend...</div>
          ) : userAccounts.length === 0 ? (
            <div className="p-4 text-sm text-neutral-500">Inga konton hittades.</div>
          ) : (
            userAccounts.map((account) => {
              const isEditing = activeEditingAccountId === account.accountId;
              const draftAccount = draftUserAccounts.find(
                (a) => a.accountId === account.accountId,
              );

              const summaryText = account.allocations
                .map(({ accountType, targetPercentage }) => `${accountType} ${targetPercentage}%`)
                .join(", ");

              return (
                <div key={account.accountId} className="border-b border-neutral-100 last:border-0">
                  <SettingsRow
                    label={account.accountName}
                    subtext={summaryText}
                    control={
                      <i
                        className={`fa-solid ${
                          isEditing ? "fa-chevron-up" : "fa-chevron-right"
                        } text-xs text-neutral-400`}
                      />
                    }
                    onClick={() => toggleAccountEditor(account.accountId)}
                  />

                  {isEditing && draftAccount && (
                    <div className="bg-neutral-50/80 p-4 rounded-lg my-2 flex flex-col gap-3 border border-neutral-200">
                      <p className="text-xs font-semibold uppercase text-neutral-500">
                        Ändra fördelning för {account.accountName}
                      </p>

                      {draftAccount.allocations.map((allocation) => (
                        <div key={allocation.id} className="flex items-center justify-between text-sm">
                          <span className="text-neutral-700 font-medium">
                            {allocation.accountType}
                          </span>
                          <div className="flex items-center gap-2">
                            <input
                              type="number"
                              min="0"
                              max="100"
                              step="0.01"
                              value={allocation.targetPercentage}
                              onChange={(event) =>
                                updateDraftPercentage(
                                  account.accountId,
                                  allocation.id,
                                  event.target.value,
                                )
                              }
                              className="w-20 rounded-md border border-neutral-300 bg-white px-2 py-1 text-right text-sm"
                            />
                            <span className="text-neutral-500">%</span>
                          </div>
                        </div>
                      ))}

                      <div className="mt-2 pt-2 border-t border-neutral-200">
                        <p
                          className={
                            activeAccountTotal === 100
                              ? "text-xs font-medium text-green-700"
                              : "text-xs font-medium text-red-600"
                          }
                        >
                          Totalt: {activeAccountTotal.toFixed(2)} % (ska vara 100 %)
                        </p>

                        {allocationError && (
                          <p role="alert" className="mt-1 text-xs text-red-600">
                            {allocationError}
                          </p>
                        )}

                        <div className="mt-3 flex gap-2 justify-end">
                          <button
                            type="button"
                            onClick={() => {
                              setActiveEditingAccountId(null);
                              setAllocationError(null);
                            }}
                            disabled={savingAllocations}
                            className="cursor-pointer rounded-md border border-neutral-300 bg-white px-3 py-1.5 text-xs font-medium hover:bg-neutral-50"
                          >
                            Avbryt
                          </button>

                          <button
                            type="button"
                            onClick={() => void saveAccountAllocations(account.accountId)}
                            disabled={savingAllocations || activeAccountTotal !== 100}
                            className="cursor-pointer rounded-md bg-neutral-900 px-3 py-1.5 text-xs font-medium text-white hover:bg-neutral-800 disabled:opacity-50"
                          >
                            {savingAllocations ? "Sparar..." : "Spara konto"}
                          </button>
                        </div>
                      </div>
                    </div>
                  )}
                </div>
              );
            })
          )}

          <SettingsRow
            label="Notis vid avvikelse"
            subtext={`Varna om fördelning avviker mer än ${deviationAlert.thresholdPercent}%`}
            control={
              <Toggle
                checked={deviationEnabled}
                onChange={(enabled) => {
                  setDeviationEnabled(enabled);
                  setDriftNotificationsEnabled(enabled);
                }}
              />
            }
          />
        </SettingsSection>

        <button
          type="button"
          onClick={() => void handleLogout()}
          disabled={loggingOut}
          className="self-start px-1 text-left text-sm font-medium text-red-600 hover:text-red-700"
        >
          {loggingOut ? "Loggar ut..." : "Logga ut"}
        </button>

        {allocationSuccess && (
          <p role="status" className="text-sm text-green-700">
            {allocationSuccess}
          </p>
        )}
      </div>
    </div>
  );
};