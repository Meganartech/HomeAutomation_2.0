import React from 'react';
import { FiHelpCircle, FiLogOut, FiMail, FiSettings, FiUser } from 'react-icons/fi';
import "@fontsource/roboto/300.css";
import '@fontsource/roboto/400.css';

export default function InsideSidePanel() {

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
                    <div className="d-flex align-items-center px-3 py-12px mb-2 sidepanel-active" style={{ ...style, fontSize: '16px' }}><FiUser className='me-2' /> Profile</div>
                    <div className="d-flex align-items-center px-3 py-12px mb-2 sidepanel-hover" style={{ ...style, fontSize: '16px' }}><FiSettings className='me-2' /> Device Settings</div>
                    <div className="d-flex align-items-center px-3 py-12px mb-2 sidepanel-hover" style={{ ...style, fontSize: '16px' }}><FiMail className='me-2' /> Message Center</div>
                    <div className="d-flex align-items-center px-3 py-12px mb-2 sidepanel-hover" style={{ ...style, fontSize: '16px' }}><FiHelpCircle className='me-2' /> FAQ & Feedback</div>
                    <div className='border-bottom mb-2'></div>
                    <div className="d-flex align-items-center px-3 py-12px mb-2 sidepanel-hover" style={{ ...style, fontSize: '16px' }}><FiLogOut className='me-2' /> Logout</div>
                </div>

            </div >
        </>
    );
};