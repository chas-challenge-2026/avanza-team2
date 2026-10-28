import { useEffect, useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/useAuth';

interface TopBarProps {
  title?: string;
  userInitials?: string;
  className?: string;
  onMenuClick?: () => void;
}

export const TopBar = ({ title, userInitials = 'JL', className, onMenuClick }: TopBarProps) => {
  const [menuOpen, setMenuOpen] = useState(false);
  const rootRef = useRef<HTMLDivElement>(null);
  const navigate = useNavigate();
  const { logout, user } = useAuth();

  useEffect(() => {
    const handleClickOutside = (e: MouseEvent) => {
      if (rootRef.current && !rootRef.current.contains(e.target as Node)) {
        setMenuOpen(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const handleLogout = () => {
    setMenuOpen(false);
    logout();
    navigate('/Loggain');
  };


  return (
    <header
      className={`relative z-50 flex items-stretch border-b border-neutral-200 bg-white${
        className ? ` ${className}` : ''
      }`}
    >
      <div className="flex flex-shrink-0 items-center gap-3 border-neutral-200 px-6 py-4 md:w-56 md:border-r">
        {onMenuClick && (
          <button
            type="button"
            onClick={onMenuClick}
            aria-label="Öppna meny"
            className="-ml-1 flex h-9 w-9 items-center justify-center rounded-full text-neutral-600 hover:bg-neutral-50 md:hidden"
          >
            <i className="fa-solid fa-bars text-lg" />
          </button>
        )}
        <i className="fa-solid fa-arrow-trend-up text-xl text-[#00c281]" />
        <span className="text-lg font-bold text-neutral-900">Min Portfölj</span>
      </div>

      <div className="flex flex-1 items-center justify-between px-6 py-4">
        {title && <span className="text-base font-semibold text-neutral-700">{title}</span>}

        <div ref={rootRef} className="relative ml-auto">
          <button
            type="button"
            onClick={() => setMenuOpen((prev) => !prev)}
            aria-haspopup="menu"
            aria-expanded={menuOpen}
            className="flex items-center gap-1.5 rounded-full p-1 hover:bg-neutral-50"
          >
            <span className="flex h-9 w-9 items-center justify-center rounded-full bg-[#00c281] text-sm font-bold text-white">
              {userInitials}
            </span>
            <i className="fa-solid fa-chevron-down text-xs text-neutral-400" />
          </button>

          {menuOpen && (
            <ul
              role="menu"
              className="absolute right-0 z-10 mt-2 min-w-[220px] overflow-hidden rounded-xl border border-neutral-200 bg-white py-1 shadow-lg"
            >
              {user && (
                <li className="border-b border-neutral-100 px-4 py-2.5">
                  <p className="truncate text-sm font-semibold text-neutral-900">{user.name}</p>
                  <p className="truncate text-xs text-neutral-500">{user.email}</p>
                </li>
              )}
              <li>
                <button
                  type="button"
                  role="menuitem"
                  onClick={handleLogout}
                  className="flex w-full items-center gap-2 px-4 py-2 text-left text-sm text-neutral-700 hover:bg-neutral-50"
                >
                  <i className="fa-solid fa-arrow-right-from-bracket text-neutral-400" />
                  Logga ut
                </button>
              </li>
            </ul>
          )}
        </div>
      </div>
    </header>
  );
};
