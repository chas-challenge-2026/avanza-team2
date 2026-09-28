import { useState } from 'react';
import { DropdownBtn } from '../DropdownBtn/DropdownBtn';

export type ActivityCategory = 'Aktiehandel' | 'Utdelningar' | 'Övrigt';

interface ActivityItem {
  label: string;
  date: string;
  icon: string;
  color: string;
  category: ActivityCategory;
}

interface RecentActivityProps {
  title?: string;
  items: ActivityItem[];
  className?: string;
}

const categoryFilterOptions = [
  { label: 'Visa alla', value: 'alla' },
  { label: 'Aktiehandel', value: 'Aktiehandel' },
  { label: 'Utdelningar', value: 'Utdelningar' },
  { label: 'Övrigt', value: 'Övrigt' },
];

export const RecentActivity = ({
  title = 'Senaste händelser',
  items,
  className,
}: RecentActivityProps) => {
  const [categoryFilter, setCategoryFilter] = useState('alla');

  const visibleItems =
    categoryFilter === 'alla'
      ? items
      : items.filter((item) => item.category === categoryFilter);

  return (
    <div
      className={`rounded-2xl border border-neutral-200 bg-white p-5${
        className ? ` ${className}` : ''
      }`}
    >
      <div className="flex items-center justify-between m-4">
        <h3 className="text-lg font-bold text-neutral-900 text-left m-4">{title}</h3>
        <DropdownBtn
          options={categoryFilterOptions}
          value={categoryFilter}
          onChange={setCategoryFilter}
          placeholder="Alla"
        />
      </div>

      <ul className="m-4 flex flex-col text-left">
        {visibleItems.map((item, index) => (
          <li key={`${item.label}-${index}`} className="flex items-center gap-3 px-4 py-4 rounded-lg hover:bg-neutral-50">
            <span
              className="flex h-8 w-8 flex-shrink-0 items-center justify-center rounded-full"
              style={{ backgroundColor: `${item.color}1a` }}
            >
              <i className={`fa-solid ${item.icon} text-xs`} style={{ color: item.color }} />
            </span>
            <span className="flex-1 text-sm font-medium text-neutral-800">{item.label}</span>
            <span className="text-xs text-neutral-400">{item.date}</span>
          </li>
        ))}
      </ul>
    </div>
  );
};
