type AltertBannerProps = {
message: string,
icon?: React.ReactNode;
};

const AltertBanner = ({
message,
icon
}: AltertBannerProps) => {

return (
<div style={styles.banner}>
<span style={styles.icon}>{icon ?? ':warning:'}</span>

<div style={styles.content}>
<span style={styles.warningTag}>Varning!</span>
<p style={styles.message}>{message}</p>
</div>
</div>
);
};

export default AltertBanner;

const styles: Record<string, React.CSSProperties> = {
banner: {
display: 'flex',
alignItems: 'flex-start',
gap: '12px',
padding: '12px 16px',
borderRadius: '8px',
backgroundColor: '#FFFBEB',
border: '1px solid #FDE68A',
margin: '8px 0',
width: '400px',
boxSizing: 'border-box',
},
icon: {
fontSize: '18px',
lineHeight: 1,
paddingTop: '2px',
},
content: {
flex: 1,
display: 'flex',
flexDirection: 'column',
gap: '2px',
},
warningTag: {
fontSize: '13px',
fontWeight: 700,
color: 'black',
},
message: {
margin: 0,
fontSize: '12px',
color: 'black',
lineHeight: 1.4,
},
};