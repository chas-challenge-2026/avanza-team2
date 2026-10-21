import type { ReactNode } from 'react';

interface SettingsSectionProps {
  title: string;
  children: ReactNode;
  className?: string;
}

export const SettingsSection = ({ title, children, className }: SettingsSectionProps) => {
  return (
    <div className={className}>
      <h2 className="mb-2 text-left text-base font-bold text-neutral-900">{title}</h2>
      <div className="divide-y divide-neutral-200 rounded-2xl border border-neutral-200 bg-white px-5">
        {children}
      </div>
    </div>
  );
};
