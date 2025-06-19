export default function ModalLayout({ title, msg, modal, children, hideClose = false }) {
    return (
        <>
            <div style={{
                position: 'fixed', top: 0, left: 0, right: 0, bottom: 0,
                backgroundColor: 'rgba(0,0,0,0.1)', display: 'flex',
                alignItems: 'center', justifyContent: 'center', zIndex: 1050
            }}>
                <div style={{
                    backgroundColor: '#fff', borderRadius: '20px',
                    width: '100%', maxWidth: '440px',
                    boxShadow: '0 10px 25px rgba(0,0,0,0.1)',
                    padding: '24px', position: 'relative', textAlign: 'center'
                }}>
                    {!hideClose && (
                        <button onClick={modal} style={{
                            position: 'absolute', top: '8px', right: '12px',
                            background: 'none', fontSize: '24px', cursor: 'pointer'
                        }} className='border-0'>&times;</button>
                    )}
                    
                    <div style={{ fontWeight: 600, fontSize: '24px' }} className='mb-3'>{title}</div>

                    {msg && (
                        <div style={{ color: "#6c757d", fontSize: '16px' }} className='mb-5'>{msg}</div>
                    )}

                    {children}
                </div>
            </div>
        </>
    );
};