import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { DropdownBtn } from '../DropdownBtn/DropdownBtn';

interface Account {
  id?: number;
  name: string;
  type: string;
  value: string;
  drifting?: boolean;
}

interface AccountsTableProps {
  accounts: Account[];
  sharpeRatio?: number | null;
  className?: string;
}

const typeFilterOptions = [
  { label: 'Visa alla', value: 'alla' },
  { label: 'ISK', value: 'ISK' },
  { label: 'KF', value: 'KF' },
  { label: 'Depå', value: 'Depå' },
];

export const AccountsTable = ({
  accounts,
  sharpeRatio,
  className,
}: AccountsTableProps) => {
  const [typeFilter, setTypeFilter] = useState('alla');
  const navigate = useNavigate();

  const visibleAccounts =
    typeFilter === 'alla'
      ? accounts
      : accounts.filter((account) => account.type === typeFilter);

  const handleAccountClick = (account: Account) => {
    const accountType = account.type.trim().toLowerCase();

    const filterValue =
      accountType === 'depå' ? 'depa' : accountType;

    navigate(`/innehav?account=${encodeURIComponent(filterValue)}`);
  };

  return (
    <div
      className={`rounded-2xl border border-neutral-200 bg-white p-5${
        className ? ` ${className}` : ''
      }`}
    >
      <div className="mb-3 flex items-center justify-between">
        <h3 className="text-lg font-bold text-neutral-900">
          Mina konton
        </h3>

        <DropdownBtn
          options={typeFilterOptions}
          value={typeFilter}
          onChange={setTypeFilter}
          placeholder="Alla"
        />
      </div>

      <table className="w-full border-collapse text-sm">
        <thead>
          <tr className="border-b border-t border-neutral-100 bg-neutral-50 text-left text-xs font-semibold text-neutral-900">
            <th className="px-4 py-2.5 font-semibold">Konto</th>
            <th className="px-4 py-2.5 font-semibold">Typ</th>
            <th className="px-4 py-2.5 font-semibold">
              Värde (SEK)
            </th>
            <th className="w-6" />
          </tr>
        </thead>

        <tbody>
          {visibleAccounts.map((account) => (
            <tr
              key={account.id ?? account.name}
              onClick={() => handleAccountClick(account)}
              onKeyDown={(event) => {
                if (event.key === 'Enter' || event.key === ' ') {
                  event.preventDefault();
                  handleAccountClick(account);
                }
              }}
              tabIndex={0}
              role="link"
              aria-label={`Visa innehav för ${account.name}`}
              className="cursor-pointer border-b border-neutral-100 text-left transition-colors last:border-0 hover:bg-neutral-50 focus-visible:outline focus-visible:outline-2 focus-visible:outline-blue-500"
            >
              <td className="px-4 py-2.5 font-medium text-neutral-900">
                <span className="inline-flex items-center gap-2">
                  {account.drifting && (
                    <span
                      className="h-2.5 w-2.5 flex-shrink-0 rounded-full bg-amber-500"
                      title="Avviker från målfördelningen"
                      role="img"
                      aria-label="Avviker från målfördelningen"
                    />
                  )}
                  {account.name}
                </span>
              </td>

              <td className="px-4 py-2.5">
                <span className="rounded-md bg-blue-50 px-2 py-0.5 text-xs font-semibold text-blue-700">
                  {account.type}
                </span>
              </td>

              <td className="px-4 py-2.5 text-neutral-900">
                {account.value}
              </td>

              <td className="px-4 py-2.5 text-right text-neutral-300">
                <i className="fa-solid fa-chevron-right text-xs" />
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      {sharpeRatio !== undefined && (
        <div className="mt-3 flex items-center justify-between border-t border-neutral-100 pt-3 text-sm">
          <span className="text-neutral-500">Sharpe-kvot (portfölj)</span>
          <span className="font-semibold text-neutral-900">
            {sharpeRatio === null
              ? 'Otillräcklig data'
              : sharpeRatio.toLocaleString('sv-SE', {
                  minimumFractionDigits: 2,
                  maximumFractionDigits: 2,
                })}
          </span>
        </div>
      )}
    </div>
  );
};