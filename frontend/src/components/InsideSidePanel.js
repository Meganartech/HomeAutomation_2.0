import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useDispatch } from 'react-redux';
import { clearUserData } from '../redux/slice';
import ModalLayout from '../components/layout/ModalLayout'
import "@fontsource/roboto/300.css";
import '@fontsource/roboto/400.css';

const customStyle = { fontFamily: 'Roboto', fontWeight: 300, lineHeight: '100%', letterSpacing: '0%' };

export default function InsideSidePanel({ menu, activePage }) {
    const [showLogoutModal, setShowLogoutModal] = useState(false);
    const navigate = useNavigate();
    const dispatch = useDispatch();

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
            <div className="min-vh-100 px-3">
                <div className="d-flex flex-column py-4">
                    {menu.map((menuObj, index) => {
                        if (menuObj.type === 'link') {
                            return (
                                <Link key={index} to={menuObj.path} style={{ ...customStyle, fontSize: '16px' }}
                                    className={`text-decoration-none text-1C1C1E d-flex align-items-center px-3 py-12px mb-2 sidepanel-hover ${activePage === menuObj.label ? 'inside-sidepanel-active' : ''}`}>
                                    <menuObj.icon className='me-2' /> {menuObj.label}
                                </Link>
                            );
                        } else if (menuObj.type === 'divider') {
                            return <div key={index} className='border-bottom mb-2'></div>;
                        } else if (menuObj.type === 'logout') {
                            return (
                                <div key={index} className="d-flex align-items-center px-3 py-12px mb-2 sidepanel-hover text-danger"
                                    style={{ ...customStyle, fontSize: '16px', cursor: 'pointer' }} onClick={() => setShowLogoutModal(true)}>
                                    <menuObj.icon className='me-2' /> Logout
                                </div>
                            );
                        }
                        return null;
                    })}
                </div>
            </div>

            {showLogoutModal && (
                <ModalLayout title={'Logout'} msg={<span>Are you sure want to <strong>Logout</strong>?</span>}
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