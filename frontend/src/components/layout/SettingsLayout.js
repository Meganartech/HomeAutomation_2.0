import { FiUser, FiLogOut } from 'react-icons/fi';
import InsideLayout from './InsideLayout';

export default function SettingsLayout({ activePage, InsideContent }) {
    const settingsMenu = [
        { type: 'link', label: 'Profile', path: '/settings/profile', icon: FiUser },
        { type: 'divider' },
        { type: 'logout', icon: FiLogOut }
    ];

    return (
        <>
            <InsideLayout menu={settingsMenu} activePage={'Settings'} insideActivePage={activePage}>
                <InsideContent />
            </InsideLayout>
        </>
    );
};