type ToggleProps = {
    onChange: (checked: boolean) => void;
    disabled?: boolean;
    checked: boolean;
    className?: string;
};

export const Toggle = (props : ToggleProps) => {
    return (
        <button 
        className = {`relative flex h-6 w-12 items-center rounded-full p-0.5 ${props.checked ? 'bg-green-500' : 'bg-gray-300'} transition-colors duration-200 ease-in-out focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 disabled:cursor-not-allowed disabled:opacity-50 ${props.className ?? ''}`}
        role = "switch"
        aria-checked = {props.checked}
        type = "button"
        onClick = {() => props.onChange(!props.checked)}
        disabled={props.disabled}
>
            <span className = {`h-5 w-5 rounded-full bg-white shadow-md transition-transform duration-200 ease-in-out ${props.checked ? 'translate-x-6' : 'translate-x-0'}`}></span>
        </button>
       
    );
};