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
    const [modal, setModal] = useState({ show: false, title: '', message: '', isError: false, onConfirm: null });
    const navigate = useNavigate();

    const token = localStorage.getItem('token');

    const fetchDevice = useCallback(async () => {
        if (!token) {
            navigate('/');
            return;
        }
        try {
            const { data, status } = await axios.get(`${process.env.REACT_APP_API_URL}/user/thing`,
                { headers: { Authorization: `Bearer ${token}` } }
            );
            if (status === 200) {
                const deviceList = data.things.filter(thingsObj => thingsObj.roomName === roomName).map(thingsObj => {
                    const mainItem = thingsObj.items?.find(item =>
                        item.type === 'Switch' || item.type === 'Color' || item.name?.toLowerCase().includes('wiz_bulb_color')) || thingsObj.items?.[0];
                    return {
                        label: thingsObj.label,
                        itemName: mainItem?.name,
                        roomName: thingsObj.roomName,
                        status: mainItem?.state === 'ON' || mainItem?.state !== '0,0,0'
                    };
                });
                setDevices(deviceList);
            }
        } catch (err) {
            setDevices([]);
            const errorMessage = err.response?.data?.error || 'Failed to fetch device';
            console.error(errorMessage);
        }
    }, [roomName, token, navigate]);

    useEffect(() => {
        const fetchRoom = async () => {
            if (!token) {
                navigate('/');
                return;
            }
            try {
                const { data, status } = await axios.get(`${process.env.REACT_APP_API_URL}/user/room`,
                    { headers: { Authorization: `Bearer ${token}` } }
                );
                if (status === 200 && data.length > 0) {
                    setHasRoom(true);
                    return;
                }
            } catch (err) {
                setHasRoom(false);
                const errorMessage = err.response?.data?.error || 'Failed to fetch room';
                console.error(errorMessage);
            }
        };
        fetchRoom();
        fetchDevice();
        const intervalId = setInterval(() => {
            fetchDevice();
        }, 3000);
        return () => clearInterval(intervalId);
    }, [fetchDevice, navigate, token]);

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
            const { data, status } = await axios.post(`${process.env.REACT_APP_API_URL}/user/room`, { roomName: normalizedRoomName },
                { headers: { Authorization: `Bearer ${token}` } }
            );
            if (status === 200) {
                setModal({
                    show: true,
                    title: 'Success',
                    message: data.message,
                    isError: false,
                    onConfirm: () => {
                        setModal({ ...modal, show: false });
                        setNewRoomName('');
                        setShowAddRoomModal(false);
                        navigate(`/room/${normalizedRoomName}`);
                    }
                });
            }
        } catch (err) {
            const errorMessage = err.response?.data?.error || 'Failed to add room';
            setModal({
                show: true,
                title: 'Failed',
                message: <span className='text-danger'>{errorMessage}</span>,
                isError: true,
                onConfirm: () => setModal({ ...modal, show: false }),
            });
        }
    };

    const handleToggle = async (itemName, newStatus) => {
        if (!token) {
            navigate('/');
            return;
        }
        try {
            const { status } = await axios.post(`${process.env.REACT_APP_API_URL}/user/control`, { itemName, command: newStatus ? 'ON' : 'OFF' },
                { headers: { Authorization: `Bearer ${token}` } }
            );
            if (status === 200) {
                fetchDevice();
            }
        } catch (err) {
            const errorMessage = err.response?.data?.error || 'Failed to send command';
            console.error(errorMessage);
        }
    };

    const confirmDeleteRoom = () => {
        setShowDeleteRoomModal(true);
    };

    const handleDeleteRoomConfirmed = async () => {
        if (!token) {
            navigate('/');
            return;
        }
        setShowDeleteRoomModal(false);
        try {
            const response1 = await axios.get(`${process.env.REACT_APP_API_URL}/user/room`,
                { headers: { Authorization: `Bearer ${token}` } }
            );
            if (response1.status === 200) {
                const rooms = response1.data;
                const roomToDelete = rooms.find(r => r.roomName === roomName);
                if (!roomToDelete) {
                    setModal({
                        show: true,
                        title: 'Error',
                        message: <span className='text-danger'>Room not found</span>,
                        isError: true,
                        onConfirm: () => setModal({ ...modal, show: false }),
                    });
                    return;
                }

                const response2 = await axios.delete(`${process.env.REACT_APP_API_URL}/user/delete/room`,
                    { headers: { Authorization: `Bearer ${token}` }, params: { roomId: roomToDelete.roomId } }
                );
                if (response2.status === 200) {
                    const response3 = await axios.get(`${process.env.REACT_APP_API_URL}/user/room`,
                        { headers: { Authorization: `Bearer ${token}` } }
                    );
                    let updatedRooms = [];
                    if (response3.status === 200) {
                        updatedRooms = response3.data;
                    }
                    setModal({
                        show: true,
                        title: 'Success',
                        message: response2.data.message,
                        isError: false,
                        onConfirm: () => {
                            setModal({ ...modal, show: false });
                            if (updatedRooms.length === 0) {
                                navigate("/room/no_room");
                            } else {
                                navigate(`/room/${updatedRooms[0].roomName}`);
                            }
                        }
                    });
                }
            }
        } catch (err) {
            const errorMessage = err.response?.data?.error || 'Failed to delete room';
            setModal({
                show: true,
                title: 'Failed',
                message: <span className='text-danger'>{errorMessage}</span>,
                isError: true,
                onConfirm: () => setModal({ ...modal, show: false }),
            });
        }
    };


    return (
        <>
            <div className="container px-5 py-4">

                <div className="d-flex justify-content-between align-items-center mb-3">
                    <div style={{ fontSize: '24px', lineHeight: '100%', letterSpacing: '0' }}>{hasRoom === false ? 'No Room' : roomName}</div>
                    <button className="btn btn-dark" onClick={() => setShowAddRoomModal(true)}>Add Room</button>
                </div>

                {/* Table */}
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
                                                    {devicesObj.label}
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
            </div >

            {/* Add Room Modal */}
            {showAddRoomModal && (
                <ModalLayout title={'Add Room'} modal={() => setShowAddRoomModal(false)}>
                    <form onSubmit={handleAddRoom}>
                        <div className="text-start mb-5">
                            <label className="form-label" htmlFor='roomName'>Room Name</label>
                            <input type="text" id='roomName' className="form-control" value={newRoomName} onChange={(e) => setNewRoomName(e.target.value)} required />
                        </div>
                        <Buttons buttonName={'Add'} onCancel={() => setShowAddRoomModal(false)} />
                    </form>
                </ModalLayout>
            )}

            {/* Delete Room Modal */}
            {showDeleteRoomModal && (
                <ModalLayout title={'Delete Room'} msg={<span>Do you really want to delete {roomName} ?</span>}
                    modal={() => setShowDeleteRoomModal(false)}>
                    <Buttons buttonName={'Delete'} onCancel={() => setShowDeleteRoomModal(false)} onDelete={handleDeleteRoomConfirmed} />
                </ModalLayout>
            )}

            {/* Alert Modal */}
            {modal.show && (
                <ModalLayout title={modal.title} msg={modal.message} modal={modal.onConfirm} hideClose={!modal.isError}>
                    <button onClick={modal.onConfirm} className={`btn btn-dark px-3`}>
                        {modal.isError ? 'Try Again' : 'OK'}
                    </button>
                </ModalLayout>
            )}
        </>
    );
};