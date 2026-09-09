import type { ElementType, ReactNode } from 'react';

type TitleVariant = "primary" | "secondary";

interface TitleProps {
  as?: ElementType; // 'h1', 'h2', etc, default is 'h2'
  variant?: TitleVariant;
  children: ReactNode;
className?: string;

}

const variantStyles: Record<TitleVariant, string> = {
  primary: 'text-xl md:text-5xl font-bold text-[#a365d8] mt-4 py-4',
  secondary: 'text-2xl md:text-3xl font-semibold text-neutral-800',
};

export const Title = ({ as: Component = 'h2', variant = 'primary', children, className,}: TitleProps) => {
  return (
    <Component className={variantStyles[variant] + (className ? ` ${className}` : '')}>
      {children}
    </Component>
  );
}

