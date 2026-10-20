import { NavLink } from 'react-router-dom';

interface NavItem {
  to: string;
  label: string;
  icon: string;
}

const mainNavItems: NavItem[] = [
  { to: '/', label: 'Översikt', icon: 'fa-house' },
  { to: '/innehav', label: 'Innehav', icon: 'fa-users' },
  { to: '/notiser', label: 'Notiser', icon: 'fa-bell' },
  { to: '/analyser', label: 'Analyser', icon: 'fa-chart-line' },
  { to: '/documents', label: 'Dokument', icon: 'fa-file-lines' },
  { to: '/installningar', label: 'Inställningar', icon: 'fa-gear' },
];



const navLinkClassName = ({ isActive }: { isActive: boolean }) =>
  `flex items-center gap-3 rounded-xl px-3 py-2.5 text-sm font-semibold transition-colors ${
    isActive
      ? 'bg-[#eafaf3] text-[#00a36c]'
      : 'text-neutral-600 hover:bg-neutral-50'
  }`;

interface NavbarProps {
  isOpen: boolean;
  onClose: () => void;
}

export const Navbar = ({ isOpen, onClose }: NavbarProps) => {
  return (
    <>
      {isOpen && (
        <div
          className="absolute inset-0 z-30 bg-black/30 md:hidden"
          onClick={onClose}
          aria-hidden="true"
        />
      )}

      <nav
        className={`absolute inset-y-0 left-0 z-40 flex h-full w-56 flex-shrink-0 flex-col justify-between overflow-y-auto border-r border-neutral-200 bg-white px-3 py-6 transition-transform duration-200 ease-in-out md:static md:z-auto md:translate-x-0 ${
          isOpen ? 'translate-x-0' : '-translate-x-full'
        }`}
      >
        <div className="flex flex-col gap-1">
          {mainNavItems.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              end={item.to === '/'}
              className={navLinkClassName}
              onClick={onClose}
            >
              <i className={`fa-solid ${item.icon} w-4 text-center`} />
              {item.label}
            </NavLink>
          ))}
        </div>
      </nav>
    </>
  );
};
