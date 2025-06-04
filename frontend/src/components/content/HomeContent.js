import axios from 'axios';
import { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { useSelector } from 'react-redux';
import { FaLightbulb } from 'react-icons/fa';

export default function HomePage() {
    const [devices, setDevices] = useState([]);
    const [rooms, setRooms] = useState([]);
    const [selectedRoom, setSelectedRoom] = useState('');
    const navigate = useNavigate();

    const userData = useSelector((state) => state.user);

    const token = localStorage.getItem('token');

    const fetchDevices = useCallback(async () => {
        if (!token) {
            navigate('/');
            return;
        }
        try {
            const { data, status } = await axios.get('http://localhost:8081/user/device',
                { headers: { Authorization: `Bearer ${token}` } }
            );
            if (status === 200) {
                const uniqueRooms = [];
                const devicesList = data.things.map(thingsObj => {
                    const mainItem = thingsObj.items?.find(itemsObj => (
                        itemsObj.type === 'Switch' || itemsObj.type === 'Color' || itemsObj.name?.toLowerCase().includes('wiz_bulb_color')));
                    if (!uniqueRooms.some(ref => ref.roomName === thingsObj.roomName)) {
                        uniqueRooms.push({ id: thingsObj.roomId, roomName: thingsObj.roomName });
                    }
                    return {
                        itemName: mainItem?.name,
                        deviceName: thingsObj.label,
                        roomName: thingsObj.roomName,
                        status: mainItem?.state === 'ON' || mainItem?.state !== '0,0,0'
                    };
                });
                setDevices(devicesList);
                setRooms(uniqueRooms);
            }
        } catch (err) {
            const error = err.response?.data?.error || 'Error fetching devices';
            console.error('Error fetching devices:', error);
            setDevices([]);
            setRooms([]);
        }
    }, [token, navigate]);

    useEffect(() => {
        fetchDevices();
        const intervalId = setInterval(() => {
            fetchDevices();
        }, 3000);
        return () => clearInterval(intervalId);
    }, [fetchDevices]);

    const handleRoomChange = (e) => {
        setSelectedRoom(e.target.value);
    };

    const filteredDevices = selectedRoom ? devices.filter(ref => ref.roomName === selectedRoom) : devices;

    return (
        <>
            <div className="container px-5 py-4">
                {/* Greeting Banner */}
                <div className="d-flex border-top border-start p-3 mb-5" style={{ minHeight: '160px', borderRadius: '10px', boxShadow: '2px 3px 10px rgba(0,0,0,0.2)' }}>
                    <div className='px-3 py-2'>
                        <div className='mb-2' style={{ fontSize: '20px', fontWeight: '700', lineHeight: '100%', letterSpacing: '0%' }}>Hello, {userData.fullName}</div>
                        <div className="text-muted">Welcome home, air quality is good and Fresh. Take a walk and have coffee.</div>
                    </div>
                </div>

                {/* Home and Room Filter */}
                <div className="d-flex justify-content-between align-items-center mb-2">
                    <div style={{ fontSize: '18px', fontWeight: '600', lineHeight: '100%', letterSpacing: '0%' }}>{userData.fullName}'s Home</div>
                    {filteredDevices.length !== 0 && (
                        <select className="form-select w-auto" onChange={handleRoomChange} value={selectedRoom || ''}>
                            {rooms.map(room => (
                                <>
                                    <option value="">All Rooms</option>
                                    <option key={room.roomId} value={room.roomId}>{room.roomName}</option>
                                </>
                            ))}
                        </select>
                    )}
                </div>

                {/* Device Cards */}
                {filteredDevices.length === 0 && (
                    <div className='alert text-center'>No devices found</div>

                )}
                <div className="row g-4">
                    {filteredDevices.map((filterDevicesObj, i) => {
                        const isActive = filterDevicesObj.status;
                        return (
                            <div key={filterDevicesObj.id || i} className="col-6 col-md-2">
                                <div className="shadow border p-3" style={{ width: '160px', height: '150px', borderRadius: '10px' }}>
                                    <div className='d-flex justify-content-between align-items-center mb-3'>
                                        <div className={`text-uppercase small ${isActive ? 'text-dark' : 'text-muted'}`}>{isActive ? 'On' : 'Off'}</div>
                                        <div className="form-check form-switch">
                                            <input className="form-check-input" type="checkbox" checked={isActive}
                                                onChange={() =>
                                                    axios.post('http://localhost:8081/user/control', {
                                                        itemName: filterDevicesObj.itemName,
                                                        command: isActive ? 'OFF' : 'ON'
                                                    }, {
                                                        headers: { Authorization: `Bearer ${token}` }
                                                    }).then(fetchDevices)
                                                }
                                            />
                                        </div>
                                    </div>

                                    <div className={`mb-3 ${isActive ? 'text-dark' : 'text-muted'}`}>
                                        <FaLightbulb className='fs-2' />
                                    </div>

                                    <div className={`mb-3 ${isActive ? 'text-dark' : 'text-muted'}`}>
                                        {filterDevicesObj.deviceName}
                                    </div>
                                </div>
                            </div>
                        );
                    })}
                </div>
            </div >
        </>
    );
};