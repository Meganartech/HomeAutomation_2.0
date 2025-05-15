import SidePanel from './SidePanel';
import Navbar from './Navbar';
import Indicator from './Indicator';
import InsideSidePanel from './InsideSidePanel';
import { FiUser, FiSettings, FiMail, FiHelpCircle, FiLogOut } from 'react-icons/fi';

export default function SettingsLayout({ activeInsidePage, InsideContent }) {

    const settingsMenu = [
        { type: 'link', label: 'Profile', path: '/settings/profile_', icon: FiUser },
        { type: 'link', label: 'Device Settings', path: '/settings/device_settings_', icon: FiSettings },
        { type: 'link', label: 'Message Center', path: '/settings/message_center_', icon: FiMail },
        { type: 'link', label: 'FAQ & Feedback', path: '/settings/faq_feedback_', icon: FiHelpCircle },
        { type: 'divider' },
        { type: 'logout', icon: FiLogOut }
    ];

    return (
        <>
            <div className="bg-eaeaea container-fluid position-fixed px-0" style={{ height: '100vh', width: '100vw' }}>

                <div className="d-flex" style={{ height: '100%' }}>

                    {/* Column-1 */}
                    <div style={{ width: '300px' }}>
                        <SidePanel activePage={'Settings'} activeState={'sidepanel-active'} />
                    </div>

                    {/* Column-2 */}
                    <div className='mx-3' style={{ width: 'calc(100% - 300px)', height: '100%', display: 'flex', flexDirection: 'column' }}>

                        <Navbar />
                        <Indicator />

                        <div className="bg-ffffff mb-3" style={{ flexGrow: 1, boxShadow: '0px 0px 24.7px 0px #00000026', borderRadius: '8px', overflow: 'hidden' }}>
                            <div className='d-flex' style={{ height: '100%' }}>

                                {/* Inside Left Panel */}
                                <div style={{ width: '240px' }}>
                                    <InsideSidePanel menu={settingsMenu} activePage={activeInsidePage} activeState={'inside-sidepanel-active'} />
                                </div>

                                {/* Inside Right Panel */}
                                <div style={{ width: 'calc(100% - 240px)', height: '100%', overflowY: 'auto' }}>
                                    {InsideContent}
                                </div>

                            </div>
                        </div>

                    </div>

                </div>

            </div>
        </>
    );
};