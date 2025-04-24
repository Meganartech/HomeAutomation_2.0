import React from 'react';
import Sidepanel from '../components/Sidepanel';
import Navbar from '../components/Navbar';

export default function Settings() {
    return (
        <>
            <div className='bg-eaeaea position-relative'>
                <div className="container-fluid">
                    <div className="row">
                        <div className="cus-col-lg-3 py-2">
                            <Sidepanel />
                        </div>

                        <div className="cus-col-lg-9 py-2">
                            <Navbar />
                        </div>
                    </div>
                </div>
            </div>
        </>
    );
};
