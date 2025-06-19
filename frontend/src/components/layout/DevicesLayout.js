import { FiUser, FiSearch, FiLogOut } from 'react-icons/fi';
import InsideLayout from "./InsideLayout";

export default function DevicesLayout({ activePage, InsideContent }) {
    const deviceMenu = [
        { type: 'link', label: 'All Devices', path: '/devices/all_devices', icon: FiUser },
        { type: 'link', label: 'Search Bindings', path: '/devices/search_bindings', icon: FiSearch },
        { type: 'divider' },
        { type: 'logout', icon: FiLogOut }
    ];

    return (
        <>
            <InsideLayout menu={deviceMenu} activePage={'Devices'} insideActivePage={activePage}>
                <InsideContent />
            </InsideLayout >
        </>
    );
};