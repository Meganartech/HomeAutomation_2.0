import { useLocation } from 'react-router-dom';
import '@fontsource/roboto/500.css';

export default function Indicator() {

    const labelMap = {

        'room': 'Rooms',
        'living_room': 'Living Room',
        'bed_room': 'Bed Room',
        'hall': 'Hall',
        'kitchen': 'Kitchen',
        'rest_room': 'Rest Room',
        'dining_room': 'Dining Room',

        'settings': 'Settings',
        'profile_': 'Profile',
        'change_password_': 'Profile',
        'device_settings_': 'Device Settings',
        'message_center_': 'Message Center',
        'faq_feedback_': 'FAQ & Feedback'
    };

    const location = useLocation();
    const pathSegments = location.pathname.split('/').filter(Boolean);
    const formatLabel = (segment) => labelMap[segment] || segment;

    const customStyle = {
        color: '#00000080',
        fontFamily: 'roboto',
        fontWeight: '500',
        fontSize: '24px',
        lineHeight: '100%',
        letterSpacing: '0',
    };

    return (
        <>
            <div style={{ boxShadow: '0px 0px 24.7px 0px #00000026', borderRadius: '8px' }} className="bg-ffffff px-5 py-4 mb-3">
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
        </>
    );
};