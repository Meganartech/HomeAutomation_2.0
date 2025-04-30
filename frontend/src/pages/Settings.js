import React from 'react';
import { FaEdit } from "react-icons/fa";
import Sidepanel from '../components/Sidepanel';
import Navbar from '../components/Navbar';
import Indicator from '../components/Indicator';
import { Link } from 'react-router-dom';

export default function Settings() {
    return (
        <>
            <div className="container-fluid position-fixed bg-eaeaea p-0">
                <div className="d-flex">
                    <div className="p-0" style={{ width: '280px' }}>
                        <Sidepanel />
                    </div>

                    <div className="p-3" style={{ width: 'calc(100% - 280px)', height: '100vh', overflowY: 'hidden' }}>
                        <div className='mb-3'>
                            <Navbar />
                        </div>
                        <Indicator />

                        <div className="bg-white p-4 rounded shadow-sm mb-3 position-relative" style={{ height: 'calc(100vh - 210px)', overflowY: 'auto' }}>
                            <div className="mt-5">
                                <Link to="/settings/profile" className="d-flex justify-content-between border p-2 mb-3 text-decoration-none text-dark">
                                    <span>Profile</span>
                                    <span style={{ color: "#6c757d" }}>{< FaEdit />}</span>
                                </Link>

                                <Link to="/settings/reset" className="d-flex justify-content-between border p-2 mb-3 text-decoration-none text-dark">
                                    <span>Reset</span>
                                    <span style={{ color: "#6c757d" }}>{<FaEdit />}</span>
                                </Link>
                            </div>
                        </div>
                    </div>
                </div>
            </div >
        </>
    );
};