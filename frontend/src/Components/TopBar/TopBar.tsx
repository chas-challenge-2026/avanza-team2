interface TopBarProps {
  title: string;
  userInitials?: string;
  className?: string;
}

export const TopBar = ({ title, userInitials = 'JL', className }: TopBarProps) => {
  return (
    <header
      className={`flex items-center justify-between border-b border-neutral-200 bg-white px-6 py-4${
        className ? ` ${className}` : ''
      }`}
    >
      <div className="flex items-center gap-2">
        <i className="fa-solid fa-arrow-trend-up text-xl text-[#00c281]" />
        <span className="text-lg font-bold text-neutral-900">{title}</span>
      </div>

      <button
        type="button"
        className="flex items-center gap-1.5 rounded-full p-1 hover:bg-neutral-50"
      >
        <span className="flex h-9 w-9 items-center justify-center rounded-full bg-[#00c281] text-sm font-bold text-white">
          {userInitials}
        </span>
        <i className="fa-solid fa-chevron-down text-xs text-neutral-400" />
      </button>
    </header>
  );
};
