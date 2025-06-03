import InsideSidePanel from '../InsideSidePanel';
import { FiUser, FiMail, FiHelpCircle, FiLogOut, FiSettings } from 'react-icons/fi';
import Layout from './Layout';

export default function SettingsLayout({ activePage, InsideContent }) {
    const settingsMenu = [
        { type: 'link', label: 'Profile', path: '/settings/profile', icon: FiUser },
        { type: 'link', label: 'Device Settings', path: '/settings/device_settings', icon: FiSettings },
        { type: 'link', label: 'Message Center', path: '/settings/message_center', icon: FiMail },
        { type: 'link', label: 'FAQ & Feedback', path: '/settings/faq_feedback', icon: FiHelpCircle },
        { type: 'divider' },
        { type: 'logout', icon: FiLogOut }
    ];

    return (
        <>
            <Layout activePage={'Settings'} activeState={'sidepanel-active'}>
                {/* Inside Left Panel */}
                <div style={{ width: '240px' }}>
                    <InsideSidePanel menu={settingsMenu} activePage={activePage} activeState={'inside-sidepanel-active'} />
                </div>

                {/* Inside Right Panel */}
                <div style={{ width: 'calc(100% - 240px)', height: '100%', overflow: 'hidden' }}>
                    <InsideContent />
                </div>
            </Layout>
        </>
    );
};