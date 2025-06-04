import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import InsideSidePanel from '../InsideSidePanel';
import { MdWeekend, MdOutlineDinnerDining, MdOutlineKitchen, MdOutlineBedroomParent } from 'react-icons/md';
import { FiLogOut } from 'react-icons/fi';
import Layout from './Layout';

const iconMap = {
    'Living Room': MdWeekend,
    'Dining Room': MdOutlineDinnerDining,
    'Kitchen': MdOutlineKitchen,
    'Bed Room': MdOutlineBedroomParent
};

export default function RoomsLayout({ roomName, InsideContent }) {
    const [roomMenu, setRoomMenu] = useState([]);
    const navigate = useNavigate();

    const fetchRoom = async () => {
        const token = localStorage.getItem('token');
        try {
            const { data, status } = await axios.get('http://localhost:8081/user/room',
                { headers: { Authorization: `Bearer ${token}` } }
            );
            if (status === 200) {
                const menu = data.map(roomObj => ({
                    type: 'link',
                    label: roomObj.roomName,
                    path: `/room/${roomObj.roomName.toLowerCase().replace(/\s+/g, '_')}`,
                    icon: iconMap[roomObj.roomName] || MdWeekend
                }));
                setRoomMenu([...menu, { type: 'divider' }, { type: 'logout', icon: FiLogOut }]);
            }
        } catch (err) {
            setRoomMenu([{ type: 'divider' }, { type: 'logout', icon: FiLogOut }]);
            const error = err.response?.data?.error || 'Room fetch failed';
            console.error('Error:', error);
        }
    };

    useEffect(() => {
        const token = localStorage.getItem('token');
        if (!token) {
            navigate('/');
        }
        fetchRoom();
    }, [navigate]);

    return (
        <>
            <Layout activePage={'Rooms'} activeState={'sidepanel-active'}>
                {/*  Inside Left Panel */}
                <div style={{ width: '240px' }}>
                    <InsideSidePanel menu={roomMenu} activePage={roomName} activeState={'inside-sidepanel-active'} />
                </div >

                {/* Inside Right Panel */}
                < div style={{ width: 'calc(100% - 240px)', height: '100%', overflowY: 'hidden' }}>
                    <InsideContent onRoomAdded={fetchRoom} />
                </div >
            </Layout>
        </>
    );
};