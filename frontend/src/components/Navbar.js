import { useState, useEffect } from 'react';
import { useSelector, useDispatch } from 'react-redux';
import { FiSettings, FiBell, FiCalendar } from 'react-icons/fi';
import { CgProfile } from 'react-icons/cg';
import { useNavigate } from 'react-router-dom';
import { clearUserData } from '../redux/slice';
import ModalLayout from '../components/layout/ModalLayout';
import '@fontsource/roboto/300.css';

function getGreeting(hour) {
    if (hour >= 5 && hour < 12) return 'Good Morning';
    if (hour >= 12 && hour < 17) return 'Good Afternoon';
    if (hour >= 17 && hour < 21) return 'Good Evening';
    return 'Good Night';
}

const customStyle = { fontFamily: 'roboto', fontWeight: '300', lineHeight: '100%', letterSpacing: '0' };

export default function Navbar() {
    const [greeting, setGreeting] = useState(getGreeting(new Date().getHours()));
    const [showLogoutModal, setShowLogoutModal] = useState(false);
    const navigate = useNavigate();

    const dispatch = useDispatch();
    const userData = useSelector((state) => state.user);

    useEffect(() => {
        const interval = setInterval(() => {
            const newGreeting = getGreeting(new Date().getHours());
            setGreeting(newGreeting);
        }, 60000);
        return () => clearInterval(interval);
    }, []);

    const handleLogout = () => {
        dispatch(clearUserData());
        localStorage.removeItem('token');
        localStorage.removeItem('role');
        localStorage.removeItem('name');
        localStorage.removeItem('userId');
        navigate('/');
        setShowLogoutModal(false);
    };

    return (
        <>
            <div className='d-flex justify-content-between bg-eaeaea my-3'>
                <div>
                    <div style={{ ...customStyle, fontSize: '20px', textTransform: 'uppercase' }}>
                        {greeting}, <strong style={{ textTransform: 'capitalize' }}>{userData.fullName}</strong>
                    </div>
                    <small style={{ ...customStyle, fontSize: '15px' }}>Your Performance, Summary This Week</small>
                </div>

                <div className="d-flex align-items-center">
                    <div className="d-flex border border-dark rounded p-2 mx-3">
                        <FiCalendar className='fs-4 me-5' />{" "}{new Date().toLocaleDateString('en-GB')}
                    </div>
                    <FiBell className='fs-4 mx-3' />
                    <CgProfile onClick={() => setShowLogoutModal(true)} className='fs-4 mx-3' style={{ cursor: 'pointer' }} />
                    <FiSettings onClick={() => navigate('/settings/profile')} className='fs-4 mx-3' style={{ cursor: 'pointer' }} />
                </div>
            </div>

            {showLogoutModal && (
                <ModalLayout title={'Logout'} msg={<span>Are you sure want to Logout ?</span>}
                    modal={() => setShowLogoutModal(false)} >
                    <div className="d-flex justify-content-around">
                        <button onClick={() => setShowLogoutModal(false)} className='btn btn-outline-eaeaea px-5'>Cancel</button>
                        <button onClick={handleLogout} className='btn btn-dark px-5'>Logout</button>
                    </div>
                </ModalLayout>
            )}
        </>
    );
};