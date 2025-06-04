import { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { FaTrash } from 'react-icons/fa';
import axios from 'axios';
import ModalLayout from '../layout/ModalLayout';

const customStyle1 = { fontWeight: '700', fontSize: '16px', lineHeight: '100%', letterSpacing: '-0.39px' };
const customStyle2 = { fontWeight: '600', fontSize: '14px', lineHeight: '100%', letterSpacing: '-0.39px' };

const Buttons = ({ buttonName, onCancel, onDelete }) => (
    <div className='d-flex justify-content-around'>
        <button type='button' className='btn btn-outline-eaeaea px-5' onClick={onCancel}>Cancel</button>
        <button type='submit' className='btn btn-dark px-5' onClick={onDelete}>{buttonName}</button>
    </div>
);

export default function RoomsContent({ roomName }) {
    const [devices, setDevices] = useState([]);
    const [hasRoom, setHasRoom] = useState(false);
    const [showAddRoomModal, setShowAddRoomModal] = useState(false);
    const [showDeleteRoomModal, setShowDeleteRoomModal] = useState(false);
    const [newRoomName, setNewRoomName] = useState('');
    const navigate = useNavigate();

    const token = localStorage.getItem('token');

    const fetchDevices = useCallback(async () => {
        if (!token) {
            navigate('/');
            return;
        }
        try {
            const { data, status } = await axios.get('http://localhost:8081/user/device', {
                headers: { Authorization: `Bearer ${token}` }
            });
            if (status === 200) {
                const deviceList = data.things.filter(thingsObj => thingsObj.roomName === roomName).map(thingsObj => {
                    const mainItem = thingsObj.items?.find(item =>
                        item.type === 'Switch' || item.type === 'Color' || item.name?.toLowerCase().includes('wiz_bulb_color')) || thingsObj.items?.[0];
                    return {
                        deviceName: thingsObj.label,
                        itemName: mainItem?.name,
                        roomName: thingsObj.roomName,
                        status: mainItem?.state === 'ON' || mainItem?.state !== '0,0,0'
                    };
                });
                setDevices(deviceList);
            }
        } catch (err) {
            const error = err.response?.data?.error || 'Error fetching devices';
            console.error('Error fetching devices:', error);
            setDevices([]);
        }
    }, [roomName, token, navigate]);

    useEffect(() => {
        const fetchRoom = async () => {
            if (!token) {
                navigate('/');
                return;
            }
            try {
                const { data, status } = await axios.get('http://localhost:8081/user/room', {
                    headers: { Authorization: `Bearer ${token}` }
                });
                if (status === 200 && data.length > 0) {
                    setHasRoom(true);
                    return;
                }
                setHasRoom(false);
            } catch (err) {
                console.error('Error checking room:', err.response?.data?.error || err.message);
                setHasRoom(false);
            }
        };
        fetchRoom();
        fetchDevices();
        const intervalId = setInterval(() => {
            fetchDevices();
        }, 3000);
        return () => clearInterval(intervalId);
    }, [fetchDevices, navigate, token]);



    const capitalize = (str) => {
        if (!str) return '';
        return str
            .split(' ')
            .map(word => word.charAt(0).toUpperCase() + word.slice(1).toLowerCase())
            .join(' ');
    };

    const handleAddRoom = async (e) => {
        e.preventDefault();
        if (!token) {
            navigate('/');
            return;
        }
        try {
            const normalizedRoomName = capitalize(newRoomName);
            const { data, status } = await axios.post('http://localhost:8081/user/room', { roomName: normalizedRoomName },
                { headers: { Authorization: `Bearer ${token}` } }
            );
            if (status === 200) {
                alert(data.message || 'Room added!');
                setNewRoomName('');
                setShowAddRoomModal(false);
                navigate(`/room/${normalizedRoomName}`);
            }

        } catch (err) {
            const error = err.response?.data?.error || 'Error adding room';
            alert(error);
        }
    };

    const handleToggle = async (itemName, newStatus) => {
        if (!token) {
            navigate('/');
            return;
        }
        try {
            const { status } = await axios.post('http://localhost:8081/user/control', { itemName, command: newStatus ? 'ON' : 'OFF' },
                { headers: { Authorization: `Bearer ${token}` } }
            );
            if (status === 200) {
                fetchDevices();
            }
        } catch (err) {
            const error = err.response?.data?.error || 'Error sending command';
            alert(error);
        }
    };

    const confirmDeleteRoom = () => {
        setShowDeleteRoomModal(true);
    };

    const handleDeleteRoomConfirmed = async () => {
        setShowDeleteRoomModal(false);
        if (!token) {
            navigate('/');
            return;
        }
        try {
            // Get list of rooms
            const response1 = await axios.get("http://localhost:8081/user/room", {
                headers: { Authorization: `Bearer ${token}` }
            });

            if (response1.status === 200) {
                const rooms = response1.data;
                const roomToDelete = rooms.find(r => r.roomName === roomName);

                if (!roomToDelete) {
                    alert("Room not found");
                    return;
                }

                // Delete the room
                const response2 = await axios.delete(`http://localhost:8081/user/room/${roomToDelete.roomId}`, {
                    headers: { Authorization: `Bearer ${token}` }
                });

                if (response2.status === 200) {
                    alert(response2.data.message);

                    // Get updated room list
                    const response3 = await axios.get("http://localhost:8081/user/room", {
                        headers: { Authorization: `Bearer ${token}` }
                    });

                    if (response3.status === 200) {
                        const updatedRooms = response3.data;
                        if (updatedRooms.length === 0) {
                            navigate("/room/no_room");
                        } else {
                            navigate(`/room/${updatedRooms[0].roomName}`);
                        }
                    } else {
                        navigate("/room/no_room");
                    }
                }
            }
        } catch (err) {
            console.error(err);
            alert(err.response?.data?.error || "Error deleting room");
        }
    };

    return (
        <>
            <div className="container px-5 py-4">
                <div className="d-flex justify-content-between align-items-center mb-3">
                    <div style={{ fontSize: '24px', lineHeight: '100%', letterSpacing: '0' }}>{hasRoom === false ? 'No Room' : roomName}</div>
                    <button className="btn btn-dark" onClick={() => setShowAddRoomModal(true)}>Add Room</button>
                </div>

                <div style={{ width: '100%', overflowX: 'hidden', height: '300px', overflowY: 'auto' }}>

                    {hasRoom === true &&
                        <div className='table-responsive'>
                            <table className='table align-middle border-0'>
                                <thead className='table-1C1C1E border-0'>
                                    <tr>
                                        <th className='p-3 border-0' style={{ ...customStyle1, width: '40%' }}>Devices</th>
                                        <th className='p-3 border-0' style={{ ...customStyle1, width: '20%' }}>Room</th>
                                        <th className='p-3 border-0' style={{ ...customStyle1, width: '20%' }}>Status</th>
                                        <th className='p-3 border-0' style={{ ...customStyle1, width: '20%' }}>Action</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {devices.length > 0 ? (
                                        devices.map((devicesObj, index) => (
                                            <tr key={index} className={index % 2 === 0 ? 'table-EAEAEA' : ''}>
                                                <td className='p-3 border-0' style={{ ...customStyle2 }}>
                                                    {devicesObj.deviceName}
                                                </td>
                                                <td className='p-3 border-0' style={{ ...customStyle2 }}>
                                                    {devicesObj.roomName}
                                                </td>
                                                <td className='p-3 border-0' style={{ ...customStyle2 }}>
                                                    <div className='form-check form-switch d-flex align-items-center'>
                                                        <input style={{ cursor: 'pointer' }} className='form-check-input' type='checkbox' checked={devicesObj.status}
                                                            onChange={() => handleToggle(devicesObj.itemName, !devicesObj.status)} />
                                                    </div>
                                                </td>
                                                <td className='p-3 border-0' style={{ ...customStyle2 }}>
                                                    <FaTrash onClick={confirmDeleteRoom} style={{ fontSize: '16px', cursor: 'pointer' }} />
                                                </td>
                                            </tr>
                                        ))
                                    ) : (
                                        <tr className='table-EAEAEA'>
                                            <td colSpan='3' className='p-3 border-0 text-center' style={{ ...customStyle2 }}>
                                                No devices found
                                            </td>
                                            <td className='p-3 border-0' style={{ ...customStyle2 }}>
                                                <FaTrash onClick={confirmDeleteRoom} style={{ fontSize: '16px', cursor: 'pointer' }} />
                                            </td>
                                        </tr>
                                    )}
                                </tbody>
                            </table>
                        </div>
                    }

                    {hasRoom === false && (
                        <div className='alert d-flex justify-content-center align-items-center h-100'>No rooms yet to create</div>
                    )}
                </div>
                {showAddRoomModal && (
                    <ModalLayout title={'Add Room'} modal={() => setShowAddRoomModal(false)}>
                        <form onSubmit={handleAddRoom}>
                            <div className="text-start mb-5">
                                <label className="form-label">Room Name</label>
                                <input type="text" className="form-control" value={newRoomName} onChange={(e) => setNewRoomName(e.target.value)} required />
                            </div>
                            <Buttons buttonName={'Add'} onCancel={() => setShowAddRoomModal(false)} />
                        </form>
                    </ModalLayout>)}

                {showDeleteRoomModal && (
                    <ModalLayout title={'Delete Room'} msg={<span>Do you really want to delete <strong>{roomName}</strong>?</span>}
                        modal={() => setShowDeleteRoomModal(false)}>
                        <Buttons buttonName={'Delete'} onCancel={() => setShowDeleteRoomModal(false)} onDelete={handleDeleteRoomConfirmed} />
                    </ModalLayout>
                )}
            </div >
        </>
    );
};