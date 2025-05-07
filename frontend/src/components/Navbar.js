import { useSelector } from 'react-redux';
import { FiSettings } from 'react-icons/fi';
import { FiBell } from 'react-icons/fi';
import { FiCalendar } from 'react-icons/fi';
import { CgProfile } from 'react-icons/cg';
import { useNavigate } from 'react-router-dom';
import '@fontsource/roboto/300.css';

export default function Navbar() {

    const customStyle = {
        fontFamily: 'roboto',
        fontWeight: '300',
        lineHeight: '100%',
        letterSpacing: '0',
    };

    const userData = useSelector((state) => state.user);

    const navigate = useNavigate();

    return (
        <>
            <div className='d-flex justify-content-between bg-eaeaea m-3'>

                <div>
                    <div style={{ ...customStyle, fontSize: '20px', textTransform: 'uppercase' }}>GOOD MORNING , <strong>{userData.fullName}</strong></div>
                    <small style={{ ...customStyle, fontSize: '15px' }}>Your Performance, Summary This Week</small>
                </div>

                <div className="d-flex align-items-center">
                    <div className="d-flex border border-dark rounded p-2 mx-3">
                        <FiCalendar className='fs-4 me-5' />{" "}{new Date().toLocaleDateString('en-GB')}
                    </div>
                    <FiBell className='fs-4 mx-3' />
                    <CgProfile onClick={() => { localStorage.removeItem("token"); localStorage.removeItem("role"); navigate("/"); }} className='fs-4 mx-3' />
                    <FiSettings className='fs-4 mx-3' />
                </div>

            </div>
        </>
    );
};