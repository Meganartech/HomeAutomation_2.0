import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useDispatch } from 'react-redux';
import { clearUserData } from '../redux/slice';
import ModalLayout from '../components/layout/ModalLayout'
import "@fontsource/roboto/300.css";
import '@fontsource/roboto/400.css';

export default function InsideSidePanel({ menu, activePage, activeState }) {
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

    const style = {
        fontFamily: 'Roboto',
        fontWeight: 300,
        lineHeight: '100%',
        letterSpacing: '0%'
    };

    return (
        <>
            <div className="min-vh-100 px-3">
                <div className="d-flex flex-column py-4">
                    {menu.map((item, index) => {
                        if (item.type === 'link') {
                            return (
                                <Link
                                    key={index}
                                    to={item.path}
                                    className={`text-decoration-none text-1C1C1E d-flex align-items-center px-3 py-12px mb-2 sidepanel-hover ${activePage === item.label ? activeState : ''}`}
                                    style={{ ...style, fontSize: '16px' }}
                                >
                                    <item.icon className='me-2' /> {item.label}
                                </Link>
                            );
                        } else if (item.type === 'divider') {
                            return <div key={index} className='border-bottom mb-2'></div>;
                        } else if (item.type === 'logout') {
                            return (
                                <div
                                    key={index}
                                    className="d-flex align-items-center px-3 py-12px mb-2 sidepanel-hover text-danger"
                                    style={{ ...style, fontSize: '16px', cursor: 'pointer' }}
                                    onClick={() => setShowLogoutModal(true)}
                                >
                                    <item.icon className='me-2' /> Logout
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