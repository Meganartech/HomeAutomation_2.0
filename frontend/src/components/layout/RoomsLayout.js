import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { MdWeekend, MdOutlineDinnerDining, MdOutlineKitchen, MdOutlineBedroomParent } from 'react-icons/md';
import { FiLogOut } from 'react-icons/fi';
import axios from 'axios';
import InsideLayout from './InsideLayout';

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
            const { data, status } = await axios.get(`${process.env.REACT_APP_API_URL}/user/room`,
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
            const errorMessage = err.response?.data?.error || 'Room fetching failed';
            console.error(errorMessage);
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
            <InsideLayout menu={roomMenu} activePage={'Rooms'} insideActivePage={roomName}>
                <InsideContent onRoomAdded={fetchRoom} />
            </InsideLayout>
        </>
    );
};