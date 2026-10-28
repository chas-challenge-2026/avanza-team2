import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { SettingsSection } from "../../Components/SettingsSection/SettingsSection.tsx";
import { SettingsRow } from "../../Components/SettingsRow/SettingsRow.tsx";
import { Toggle } from "../../Components/Toggle/Toggle.tsx";
import {
  deviationAlert,
} from "./settingsSampleData.ts";
import {
  getTargetAllocations,
  updateTargetAllocations,
  type TargetAllocation,
} from "../../Services/targetAllocationService";
import {
  areDriftNotificationsEnabled,
  setDriftNotificationsEnabled,
} from "../../utils/driftNotificationPreference.ts";
import { useAuth } from "../../context/useAuth.ts";

export const Settings = () => {
  const navigate = useNavigate();
  const { logout } = useAuth();
  const [loggingOut, setLoggingOut] = useState(false);

  // Notification settings
  const [deviationEnabled, setDeviationEnabled] = useState(
    areDriftNotificationsEnabled,
  );

  // Target allocation state
  const [allocations, setAllocations] = useState<TargetAllocation[]>([]);
  const [draftAllocations, setDraftAllocations] = useState<TargetAllocation[]>(
    [],
  );
  const [editingAllocations, setEditingAllocations] = useState(false);
  const [loadingAllocations, setLoadingAllocations] = useState(true);
  const [savingAllocations, setSavingAllocations] = useState(false);
  const [allocationError, setAllocationError] = useState<string | null>(null);
  const [allocationSuccess, setAllocationSuccess] = useState<string | null>(
    null,
  );

  // Calculate the total percentage of the current draft.
  const allocationTotal = useMemo(
    () =>
      draftAllocations.reduce(
        (sum, allocation) => sum + allocation.targetPercentage,
        0,
      ),
    [draftAllocations],
  );

  // Load the authenticated user's target allocations from the backend.
  useEffect(() => {
    let isMounted = true;

    const loadAllocations = async () => {
      try {
        setLoadingAllocations(true);
        setAllocationError(null);

        const data = await getTargetAllocations();

        if (isMounted) {
          setAllocations(data);
          setDraftAllocations(data);
        }
      } catch {
        if (isMounted) {
          setAllocationError("Kunde inte hämta målfördelningen.");
        }
      } finally {
        if (isMounted) {
          setLoadingAllocations(false);
        }
      }
    };

    void loadAllocations();

    // Avoid updating component state after unmounting.
    return () => {
      isMounted = false;
    };
  }, []);

  // Open the editor using a copy of the saved allocations.
  const openAllocationEditor = () => {
    setDraftAllocations(allocations.map((allocation) => ({ ...allocation })));
    setAllocationError(null);
    setAllocationSuccess(null);
    setEditingAllocations(true);
  };

  const toggleAllocationEditor = () => {
    if (savingAllocations) {
      return;
    }

    if (editingAllocations) {
      setEditingAllocations(false);
      setAllocationError(null);
      return;
    }

    openAllocationEditor();
  };

  // Update the percentage for a specific account type.
  const updateDraftPercentage = (id: number, value: string) => {
    // Allow an empty input while the user is editing.
    if (value.trim() === "") {
      setDraftAllocations((current) =>
        current.map((allocation) =>
          allocation.id === id
            ? { ...allocation, targetPercentage: 0 }
            : allocation,
        ),
      );
      return;
    }

    const percentage = Number(value);

    // Only accept valid percentages between 0 and 100.
    if (!Number.isFinite(percentage) || percentage < 0 || percentage > 100) {
      return;
    }

    setDraftAllocations((current) =>
      current.map((allocation) =>
        allocation.id === id
          ? { ...allocation, targetPercentage: percentage }
          : allocation,
      ),
    );

    setAllocationError(null);
  };

  // Save the complete allocation list to the backend.
  const saveAllocations = async () => {
    // Require at least one allocation and a total of exactly 100%.
    if (draftAllocations.length === 0 || allocationTotal !== 100) {
      setAllocationError(
        "Målfördelningen måste innehålla minst en kontotyp och summera till exakt 100 %.",
      );
      return;
    }

    try {
      setSavingAllocations(true);
      setAllocationError(null);
      setAllocationSuccess(null);

      // Send the complete allocation list in the format expected by the backend.
      const updated = await updateTargetAllocations(
        draftAllocations.map(({ accountType, targetPercentage }) => ({
          accountType,
          targetPercentage,
        })),
      );

      setAllocations(updated);
      setDraftAllocations(updated);
      setEditingAllocations(false);
      setAllocationSuccess("Målfördelningen har sparats.");
    } catch {
      setAllocationError(
        "Kunde inte spara målfördelningen. Kontrollera anslutningen och försök igen.",
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
        {/* General settings */}
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

        {/* Target allocation editor */}
        {/* Portfolio goals */}
        <SettingsSection title="Portföljmål">
          <SettingsRow
            label="Redigera målfördelning"
            subtext={
              loadingAllocations
                ? "Hämtar målfördelning..."
                : allocations.length > 0
                  ? allocations
                      .map(
                        ({ accountType, targetPercentage }) =>
                          `${accountType} ${targetPercentage}%`,
                      )
                      .join(", ")
                  : "Ingen målfördelning tillgänglig"
            }
            control={
              <i
                className={`fa-solid ${editingAllocations ? "fa-chevron-up" : "fa-chevron-right"} text-xs text-neutral-400`}
              />
            }
            onClick={toggleAllocationEditor}
          />

          {editingAllocations && (
            <div className="py-2">
              {draftAllocations.map((allocation) => (
                <SettingsRow
                  key={allocation.id}
                  label={allocation.accountType}
                  control={
                    <div className="flex items-center gap-2">
                      <input
                        type="number"
                        min="0"
                        max="100"
                        step="0.01"
                        value={allocation.targetPercentage}
                        onChange={(event) =>
                          updateDraftPercentage(allocation.id, event.target.value)
                        }
                        className="w-20 rounded-md border border-neutral-300 px-2 py-1 text-right text-sm"
                        aria-label={`Målfördelning för ${allocation.accountType}`}
                      />

                      <span className="text-sm text-neutral-500">%</span>
                    </div>
                  }
                />
              ))}

              <div className="px-4 py-3 text-sm">
                <p
                  className={
                    allocationTotal === 100 ? "text-green-700" : "text-red-600"
                  }
                >
                  Totalt: {allocationTotal.toFixed(2)} %
                </p>

                {allocationError && (
                  <p role="alert" className="mt-2 text-red-600">
                    {allocationError}
                  </p>
                )}

                <div className="mt-4 flex gap-3">
                  <button
                    type="button"
                    onClick={() => {
                      setEditingAllocations(false);
                      setAllocationError(null);
                    }}
                    disabled={savingAllocations}
                    className="cursor-pointer rounded-md border border-neutral-300 px-4 py-2 text-sm hover:bg-neutral-50 disabled:cursor-not-allowed disabled:opacity-50"
                  >
                    Avbryt
                  </button>

                  <button
                    type="button"
                    onClick={() => void saveAllocations()}
                    disabled={
                      savingAllocations ||
                      loadingAllocations ||
                      allocationTotal !== 100 ||
                      draftAllocations.length === 0
                    }
                    className="cursor-pointer rounded-md bg-neutral-900 px-4 py-2 text-sm text-white hover:bg-neutral-800 disabled:cursor-not-allowed disabled:opacity-50"
                  >
                    {savingAllocations ? "Sparar..." : "Spara"}
                  </button>
                </div>
              </div>
            </div>
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

        {/* Logout */}
        <button
          type="button"
          onClick={() => void handleLogout()}
          disabled={loggingOut}
          className="self-start px-1 text-left text-sm font-medium text-red-600 hover:text-red-700"
        >
          {loggingOut ? "Loggar ut..." : "Logga ut"}
        </button>

        {/* Display loading errors and save confirmations outside the editor. */}
        {!editingAllocations && allocationError && (
          <p role="alert" className="text-sm text-red-600">
            {allocationError}
          </p>
        )}

        {!editingAllocations && allocationSuccess && (
          <p role="status" className="text-sm text-green-700">
            {allocationSuccess}
          </p>
        )}
      </div>
    </div>
  );
};
