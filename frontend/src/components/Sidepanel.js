import { MdDashboard, MdDevices, MdOutlineSolarPower } from 'react-icons/md';
import { FaDoorOpen } from 'react-icons/fa';
import { BiBarChartAlt2 } from 'react-icons/bi';
import { FiSettings } from 'react-icons/fi';

import Profile from '../assets/Profile.png';

export default function Sidepanel() {
    return (
        <>
            <div className="bg-2a2a2c text-white min-vh-100 pt-5 px-3">
                <p className="text-center">Meganar Technologies</p>
                <hr />
                <div className="d-flex align-items-center">
                    <img src={Profile} alt="Profile" width={'40px'} height={'40px'} className="rounded-circle mx-3" />
                    <span className='lead fs-6'>Brooklyn Alice</span>
                </div>
                <hr />
                <ul className="d-flex flex-column ms-3 list-unstyled">
                    <div className="d-flex mb-4 px-3 py-2"><MdDashboard className='fs-5 me-2' /> Dashboard</div>

                    <div className="d-flex mb-4 px-3 py-2"><FaDoorOpen className='fs-5 me-2' /> Rooms</div>

                    <div className="d-flex mb-4 px-3 py-2"><MdDevices className='fs-5 me-2' /> Devices</div>

                    <div className="d-flex mb-4 px-3 py-2"><MdOutlineSolarPower className='fs-5 me-2' /> Power Consumption</div>

                    <div className="d-flex mb-4 px-3 py-2"><BiBarChartAlt2 className='fs-5 me-2' /> Analytics</div>

                    <div className="d-flex mb-4 px-3 py-2 sidepanel-active"><FiSettings className='fs-5 me-2' /> Settings</div>
                </ul>
            </div>
        </>
    );
};