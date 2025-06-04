import { useLocation } from 'react-router-dom';
import '@fontsource/roboto/500.css';

const customStyle = { color: '#00000080', fontFamily: 'roboto', fontWeight: '500', fontSize: '24px', lineHeight: '100%', letterSpacing: '0' };

export default function Indicator() {
    const labelMap = {
        'home': 'Home',

        'room': 'Rooms',
        'no_room': 'No Room',
        'living_room': 'Living Room',
        'bed_room': 'Bed Room',
        'hall': 'Hall',
        'kitchen': 'Kitchen',
        'rest_room': 'Rest Room',
        'dining_room': 'Dining Room',

        'devices': 'Devices',
        'all_devices': 'All Devices',
        'search_bindings': 'Search Bindings',

        'schedule': 'Schedule',
        'your_scenes': 'Your Scenes',
        'create_scenes': 'Create Scenes',

        'settings': 'Settings',
        'profile': 'Profile',
        'change_password': 'Profile',
        'device_settings': 'Device Settings',
        'message_center': 'Message Center',
        'faq_feedback': 'FAQ & Feedback'
    };

    const location = useLocation();
    const pathSegments = location.pathname.split('/').filter(Boolean);
    const toTitleCase = str => str.replace(/\w\S*/g, txt => txt.charAt(0).toUpperCase() + txt.slice(1).toLowerCase());

    const formatLabel = (segment) => {
        const decoded = decodeURIComponent(segment);
        return labelMap[decoded] || toTitleCase(decoded.replace(/_/g, ' '));
    };

    return (
        <>
            <div style={{ boxShadow: '0px 0px 24.7px 0px #00000026', borderRadius: '8px' }} className="bg-ffffff px-5 py-4 mb-3">
                <div style={{ ...customStyle }}>
                    {pathSegments.length === 0 ? (
                        <span style={{ color: '#000000' }}>{formatLabel('/')}</span>
                    ) : (
                        <>
                            <span style={{
                                color: formatLabel(pathSegments[0]) === 'Home' ? '#000000' : '#00000080',
                                fontWeight: '500'
                            }}>
                                {formatLabel(pathSegments[0])}
                            </span>
                            {pathSegments[1] && (
                                <>
                                    &nbsp;›&nbsp;
                                    <span style={{ color: '#000000' }}>{formatLabel(pathSegments[1])}</span>
                                </>
                            )}
                        </>
                    )}
                </div>
            </div>
        </>
    );
};