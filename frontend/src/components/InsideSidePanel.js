import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import "@fontsource/roboto/300.css";
import '@fontsource/roboto/400.css';

export default function InsideSidePanel({ menu, activePage, activeState }) {
    const [showLogoutModal, setShowLogoutModal] = useState(false);
    const navigate = useNavigate();

    const handleLogout = () => {
        localStorage.removeItem('token');
        localStorage.removeItem('role');
        localStorage.removeItem('user');
        navigate('/user/login');
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
                <div style={{
                    position: "fixed", top: 0, left: 0, right: 0, bottom: 0,
                    backgroundColor: "rgba(0,0,0,0.5)", display: "flex",
                    alignItems: "center", justifyContent: "center", zIndex: 1050
                }}>
                    <div style={{
                        backgroundColor: "#fff", borderRadius: "20px", width: "100%",
                        maxWidth: "440px", boxShadow: "0 10px 25px rgba(0,0,0,0.2)",
                        padding: "24px", position: "relative", textAlign: "center"
                    }}>
                        <button onClick={() => setShowLogoutModal(false)} style={{
                            position: "absolute", top: "8px", right: "12px",
                            background: 'none', fontSize: '24px', cursor: "pointer"
                        }} className='border-0'>&times;</button>

                        <div style={{ fontWeight: 600, fontSize: '24px' }} className='mb-3'>Logout</div>
                        <div style={{ color: "#6c757d", fontSize: '16px' }} className='mb-5'>Do you really want to logout ?</div>

                        <div className="d-flex justify-content-around">
                            <button onClick={() => setShowLogoutModal(false)} className='btn btn-outline-dark px-5'>Cancel</button>
                            <button onClick={handleLogout} className='btn btn-dark px-5'>Logout</button>
                        </div>
                    </div>
                </div>
            )}
        </>
    );
};