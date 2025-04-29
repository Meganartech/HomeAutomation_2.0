import { FiSettings } from 'react-icons/fi';
import { IoMdNotificationsOutline } from 'react-icons/io';
import { BsCalendar4Week } from 'react-icons/bs';
import { CgProfile } from 'react-icons/cg';
import { useSelector } from 'react-redux';
import { useNavigate } from 'react-router-dom';

export default function Navbar() {
    const userData = useSelector((state) => state.user);
    const navigate = useNavigate();

    return (
        <>
            <div className='d-flex justify-content-between bg-eaeaea'>
                <div>
                    <h5 className="fw-normal">GOOD MORNING, <strong>{userData.name}</strong></h5>
                    <small>Your Performance, Summary This Week</small>
                </div>
                <div className="d-flex align-items-center">
                    <div className="d-flex border border-dark rounded px-3 py-2 mx-3">
                        <BsCalendar4Week className='fs-5 me-5' />{" "}{new Date().toLocaleDateString()}
                    </div>
                    <IoMdNotificationsOutline className='fs-5 mx-3' />
                    <CgProfile onClick={() => { localStorage.removeItem("token"); localStorage.removeItem("role"); navigate("/"); }} className='fs-5 mx-3' />
                    <FiSettings className='fs-5 mx-3' />
                </div>
            </div>
        </>
    );
};