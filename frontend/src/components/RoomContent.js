import axios from 'axios';
import { useState, useEffect, useCallback } from 'react';

export default function RoomContent({ roomName }) {
    const [devices, setDevices] = useState([]);
    const [showAddRoomModal, setShowAddRoomModal] = useState(false);
    const [newRoomName, setNewRoomName] = useState('');

    const token = localStorage.getItem('token');

    const fetchDevices = useCallback(async () => {
        try {
            const res = await axios.get(`http://localhost:8081/user/device/list?room=${roomName}`, {
                headers: { Authorization: `Bearer ${token}` }
            });
            setDevices(res.data);
        } catch (err) {
            console.error('Error loading devices', err);
        }
    }, [roomName, token]);

    useEffect(() => {
        fetchDevices();
    }, [fetchDevices]);

    const handleAddRoom = async (e) => {
        e.preventDefault();
        try {
            await axios.post('http://localhost:8081/user/room', { roomName: newRoomName }, {
                headers: { Authorization: `Bearer ${token}` }
            });
            alert('Room added!');
            setNewRoomName('');
            setShowAddRoomModal(false);
        } catch (err) {
            alert('Error adding room');
        }
    };

    return (
        <div className="container px-5 py-4">
            <div className="d-flex justify-content-between align-items-center mb-3">
                <div style={{ fontSize: '24px' }}>{roomName}</div>
                <button className="btn btn-dark" onClick={() => setShowAddRoomModal(true)}>Add Room</button>
            </div>

            <div className="table-responsive">
                <table className="table align-middle border-0">
                    <thead className="table-1C1C1E border-0">
                        <tr>
                            <th>Room</th>
                            <th>Device</th>
                            <th>Power</th>
                            <th>Status</th>
                        </tr>
                    </thead>
                    <tbody>
                        {/* Room name row */}
                        <tr>
                            <td>{roomName}</td>
                            <td colSpan="3"></td> {/* Empty cells to align with the other columns */}
                        </tr>

                        {/* Device rows */}
                        {devices.map((d, i) => (
                            <tr key={i}>
                                <td></td> {/* Empty cell for room name */}
                                <td>{d.name}</td>
                                <td>{d.power}</td>
                                <td>
                                    <input type="checkbox" checked={d.status} readOnly />
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>

            {/* Modal to Add Room */}
            {showAddRoomModal && (
                <div className="modal-overlay">
                    <div className="modal-content">
                        <button onClick={() => setShowAddRoomModal(false)} className="close-btn">&times;</button>
                        <h4>Add Room</h4>
                        <form onSubmit={handleAddRoom}>
                            <input type="text" value={newRoomName} onChange={(e) => setNewRoomName(e.target.value)} className="form-control mb-3" />
                            <div className="d-flex justify-content-around">
                                <button className="btn btn-outline-dark" type="button" onClick={() => setShowAddRoomModal(false)}>Cancel</button>
                                <button className="btn btn-dark" type="submit">Save</button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </div>
    );
};