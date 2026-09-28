import { useState } from 'react';
import { DropdownBtn } from '../DropdownBtn/DropdownBtn';

interface Account {
  name: string;
  type: string;
  value: string;
}

interface AccountsTableProps {
  accounts: Account[];
  className?: string;
}

const typeFilterOptions = [
  { label: 'Visa alla', value: 'alla' },
  { label: 'ISK', value: 'ISK' },
  { label: 'KF', value: 'KF' },
  { label: 'Depå', value: 'Depå' },
];

export const AccountsTable = ({ accounts, className }: AccountsTableProps) => {
  const [typeFilter, setTypeFilter] = useState('alla');

  const visibleAccounts =
    typeFilter === 'alla' ? accounts : accounts.filter((account) => account.type === typeFilter);

  return (
    <div
      className={`rounded-2xl border border-neutral-200 bg-white p-5${
        className ? ` ${className}` : ''
      }`}
    >
      <div className="flex items-center justify-between m-4">
        <h3 className="text-lg font-bold text-neutral-900">Konton</h3>
        <DropdownBtn
          options={typeFilterOptions}
          value={typeFilter}
          onChange={setTypeFilter}
          placeholder="Alla"
        />
      </div>

      <table className="mt-4 w-full border-collapse text-sm">
        <thead>
          <tr className="border-b border-t border-neutral-100 text-left text-xs font-semibold text-neutral-900 px-4 py-4 bg-neutral-50">
            <th className="px-4 py-4 font-semibold">Konto</th>
            <th className="px-4 py-4 font-semibold">Typ</th>
            <th className="px-4 py-4 font-semibold">Värde (SEK)</th>
            <th className="w-6" />
          </tr>
        </thead>
        <tbody>
          {visibleAccounts.map((account) => (
            <tr key={account.name} className="text-left border-b border-neutral-100 last:border-0 hover:bg-neutral-50">
              <td className="px-4 py-4  font-medium text-neutral-900">{account.name}</td>
              <td className="px-4 py-4 ">
                <span className="rounded-md bg-blue-50 px-2 py-0.5 text-xs font-semibold text-blue-700">
                  {account.type}
                </span>
              </td>
              <td className="px-4 py-4   text-neutral-900">{account.value}</td>
              <td className="px-4 py-4  text-right text-neutral-300">
                <i className="fa-solid fa-chevron-right text-xs" />
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};
