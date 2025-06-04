import InsideSidePanel from "../InsideSidePanel";
import { FiUser, FiSearch, FiLogOut } from 'react-icons/fi';
import Layout from "./Layout";

export default function DevicesLayout({ activePage, InsideContent }) {
    const deviceMenu = [
        { type: 'link', label: 'All Devices', path: '/devices/all_devices', icon: FiUser },
        { type: 'link', label: 'Search Bindings', path: '/devices/search_bindings', icon: FiSearch },
        { type: 'divider' },
        { type: 'logout', icon: FiLogOut }
    ];

    return (
        <>
            <Layout activePage={'Devices'} activeState={'sidepanel-active'}>
                {/* Inside Left Panel */}
                <div style={{ width: '240px' }}>
                    <InsideSidePanel menu={deviceMenu} activePage={activePage} activeState={'inside-sidepanel-active'} />
                </div>

                {/* Inside Right Panel */}
                <div style={{ width: 'calc(100% - 240px)', height: '100%', overflowY: 'hidden' }}>
                    <InsideContent />
                </div>
            </Layout>
        </>
    );
};