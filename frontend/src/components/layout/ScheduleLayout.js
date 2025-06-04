import InsideSidePanel from "../InsideSidePanel";
import { FiUser, FiPlus, FiLogOut } from 'react-icons/fi';
import Layout from './Layout';

export default function ScheduleLayout({ activePage, InsideContent }) {
    const scheduleMenu = [
        { type: 'link', label: 'Your Scenes', path: '/schedule/your_scenes', icon: FiUser },
        { type: 'link', label: 'Create Scenes', path: '/schedule/create_scenes', icon: FiPlus },
        { type: 'divider' },
        { type: 'logout', icon: FiLogOut }
    ];

    return (
        <>
            <Layout activePage={'Schedule'} activeState={'sidepanel-active'}>
                {/* Inside Left Panel */}
                <div style={{ width: '240px' }}>
                    <InsideSidePanel menu={scheduleMenu} activePage={activePage} activeState={'inside-sidepanel-active'} />
                </div >

                {/* Inside Right Panel */}
                <div style={{ width: 'calc(100% - 240px)', height: '100%', overflowY: 'hidden' }}>
                    <InsideContent />
                </div>
            </Layout>
        </>
    );
};