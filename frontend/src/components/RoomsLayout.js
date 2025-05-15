import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import SidePanel from './SidePanel';
import Navbar from './Navbar';
import Indicator from './Indicator';
import InsideSidePanel from './InsideSidePanel';
import { MdWeekend, MdOutlineDinnerDining, MdOutlineKitchen, MdOutlineBedroomParent } from 'react-icons/md';
import { FiLogOut } from 'react-icons/fi';

const iconMap = {
    'Living Room': MdWeekend,
    'Dining Room': MdOutlineDinnerDining,
    'Kitchen': MdOutlineKitchen,
    'Bed Room': MdOutlineBedroomParent
};

export default function RoomsLayout({ roomName, ContentComponent }) {
    const [roomMenu, setRoomMenu] = useState([]);
    const navigate = useNavigate();

    const fetchRooms = async () => {
        const token = localStorage.getItem('token');
        try {
            const response = await axios.get('http://localhost:8081/user/room/list', {
                headers: { Authorization: `Bearer ${token}` }
            });

            const menuItems = response.data.map(room => ({
                type: 'link',
                label: room.roomName,
                path: `/room/${room.roomName.toLowerCase().replace(/\s+/g, '_')}`,
                icon: iconMap[room.roomName] || MdWeekend
            }));

            setRoomMenu([...menuItems, { type: 'divider' }, { type: 'logout', icon: FiLogOut }]);
        } catch (err) {
            console.error('Error fetching room menu:', err);
            setRoomMenu([
                { type: 'divider' },
                { type: 'logout', icon: FiLogOut }
            ]);
        }
    };

    useEffect(() => {
        const token = localStorage.getItem('token');
        if (!token) {
            navigate('/');
        }
        fetchRooms();
    }, [navigate]);

    return (
        <div className="bg-eaeaea container-fluid position-fixed px-0" style={{ height: '100vh', width: '100vw' }}>

            <div className="d-flex" style={{ height: '100%' }}>

                {/* Column-1 */}
                <div style={{ width: '300px' }}>
                    <SidePanel activePage={'Rooms'} activeState={'sidepanel-active'} />
                </div>

                {/* Column-2 */}
                <div className='mx-3' style={{ width: 'calc(100% - 300px)', height: '100%', display: 'flex', flexDirection: 'column' }}>

                    <Navbar />
                    <Indicator />

                    <div className="bg-ffffff mb-3" style={{ flexGrow: 1, boxShadow: '0px 0px 24.7px 0px #00000026', borderRadius: '8px', overflow: 'hidden' }}>
                        <div className='d-flex' style={{ height: '100%' }}>

                            {/*  Inside Left Panel */}
                            <div style={{ width: '240px' }}>
                                <InsideSidePanel menu={roomMenu} activePage={roomName} activeState="inside-sidepanel-active" />
                            </div>

                            {/* Inside Right Panel */}
                            <div style={{ width: 'calc(100% - 240px)', height: '100%', overflowY: 'auto' }}>
                                <ContentComponent onRoomAdded={fetchRooms} />
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};