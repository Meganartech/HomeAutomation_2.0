import { FiSettings } from 'react-icons/fi';
import { IoMdNotificationsOutline } from 'react-icons/io';
import { BsCalendar4Week } from 'react-icons/bs';
import { CgProfile } from 'react-icons/cg';

export default function Navbar() {

    return (
        <>
            <div className='d-flex justify-content-between bg-eaeaea'>
                <div>
                    <h5 className="fw-normal">GOOD MORNING, <strong>RISHI</strong></h5>
                    <small>Your Performance, Summary This Week</small>
                </div>
                <div className="d-flex align-items-center">
                    <div className="d-flex border border-dark rounded px-3 py-2 mx-3">
                        <BsCalendar4Week className='fs-5 me-5' />{" "}{new Date().toLocaleDateString()}
                    </div>
                    <IoMdNotificationsOutline className='fs-5 mx-3' />
                    <CgProfile className='fs-5 mx-3' />
                    <FiSettings className='fs-5 mx-3' />
                </div>
            </div>
        </>
    );
};