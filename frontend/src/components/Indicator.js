import { useLocation } from 'react-router-dom';
import '@fontsource/roboto/500.css';

export default function Indicator() {
    const location = useLocation();

    const labelMap = {
        settings: 'Settings',
        'profile': 'Profile',
        'change-password': 'Profile'
    };

    const customStyle = {
        color: '#00000080',
        fontFamily: 'roboto',
        fontWeight: '500',
        fontSize: '24px',
        lineHeight: '100%',
        letterSpacing: '0',
    };

    const pathSegments = location.pathname.split('/').filter(Boolean);
    const formatLabel = (segment) => labelMap[segment] || segment;

    return (
        <div style={{ boxShadow: '0px 0px 24.7px 0px #00000026', borderRadius: '8px' }} className="bg-ffffff px-5 py-4 m-3">
            <div style={{ ...customStyle }}>
                {formatLabel(pathSegments[0])}
                {pathSegments[1] && (
                    <>
                        &nbsp;›&nbsp;
                        <span style={{ color: '#000000' }}>{formatLabel(pathSegments[1])}</span>
                    </>
                )}
            </div>
        </div>
    );
};