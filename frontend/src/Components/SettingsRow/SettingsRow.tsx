import type { ReactNode } from 'react';

interface SettingsRowProps {
  label: string;
  subtext?: string;
  control: ReactNode;
  onClick?: () => void;
  className?: string;
}

export const SettingsRow = ({ label, subtext, control, onClick, className }: SettingsRowProps) => {
  const content = (
    <>
      <div className="flex flex-col items-start gap-0.5 text-left">
        <span className="text-sm font-medium text-neutral-900">{label}</span>
        {subtext && <span className="text-xs text-neutral-500">{subtext}</span>}
      </div>
      <div className="flex shrink-0 items-center">{control}</div>
    </>
  );

  const rowClassName = `flex w-full items-center justify-between py-3 text-sm${
    className ? ` ${className}` : ''
  }`;

  if (onClick) {
    return (
      <button type="button" onClick={onClick} className={`${rowClassName} text-left hover:bg-neutral-50`}>
        {content}
      </button>
    );
  }

  return <div className={rowClassName}>{content}</div>;
};
