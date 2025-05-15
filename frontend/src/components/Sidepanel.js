import { Link } from 'react-router-dom';
import { useSelector } from 'react-redux';
import { MdHome, MdDevices, MdSchedule } from 'react-icons/md';
import { FaDoorOpen } from 'react-icons/fa';
import { FiSettings } from 'react-icons/fi';
import Profile from '../assets/Profile.png';
import "@fontsource/roboto/300.css";
import '@fontsource/roboto/400.css';

export default function SidePanel({ activePage, activeState }) {

    const style = {
        fontFamily: 'Roboto',
        fontWeight: 300,
        lineHeight: '100%',
        letterSpacing: '0%'
    };

    const userData = useSelector((state) => state.user);

    return (
        <>
            <div className="bg-1C1C1E text-white min-vh-100 px-3">

                <div className="text-center py-4" style={{ ...style, fontSize: '20px' }}>SMART HOME</div>

                <div className="d-flex align-items-center border-top border-bottom p-3" >
                    <img src={Profile} alt="Profile" width={'35px'} height={'35px'} className="rounded-circle me-3" />
                    <span style={{ ...style, fontSize: '20px', fontWeight: '400' }}>{userData.fullName}</span>
                </div>

                <div className="d-flex flex-column py-4">
                    <Link to="/" className={`text-decoration-none text-eaeaea d-flex align-items-center px-3 py-12px mb-3 sidepanel-hover ${activePage === 'Home' ? activeState : ''}`} style={{ ...style, fontSize: '16px' }}><MdHome className='me-2' /> Home</Link>
                    <Link to="/room/living_room" className={`text-decoration-none text-eaeaea d-flex align-items-center px-3 py-12px mb-3 sidepanel-hover ${activePage === 'Rooms' ? activeState : ''}`} style={{ ...style, fontSize: '16px' }}><FaDoorOpen className='me-2' /> Rooms</Link>
                    <Link to="/" className={`text-decoration-none text-eaeaea d-flex align-items-center px-3 py-12px mb-3 sidepanel-hover ${activePage === 'Devices' ? activeState : ''}`} style={{ ...style, fontSize: '16px' }}><MdDevices className='me-2' /> Devices</Link>
                    <Link to="/" className={`text-decoration-none text-eaeaea d-flex align-items-center px-3 py-12px mb-3 sidepanel-hover ${activePage === 'Schedule' ? activeState : ''}`} style={{ ...style, fontSize: '16px' }}><MdSchedule className='me-2' /> Schedule</Link>
                    <Link to="/settings/profile_" className={`text-decoration-none text-eaeaea d-flex align-items-center px-3 py-12px mb-3 sidepanel-hover ${activePage === 'Settings' ? activeState : ''}`} style={{ ...style, fontSize: '16px' }}><FiSettings className='me-2' /> Settings</Link>
                </div>

            </div >
        </>
    );
};