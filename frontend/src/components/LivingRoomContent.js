import axios from 'axios';
import { useState } from 'react';

export default function LivingRoomContent({ onRoomAdded }) {

    const [showAddRoomModal, setShowAddRoomModal] = useState(false);
    const [roomName, setRoomName] = useState('');

    const devices = [
        { name: 'Ambient Smart Lights', room: 'Living room', power: '45KWh', status: true },
        { name: 'Smart Air Purifier', room: 'Living Room', power: '45KWh', status: false },
        { name: 'Smart Light Strip', room: 'Living room', power: '45KWh', status: true },
        { name: 'Smart Thermostat', room: 'Living room', power: '45KWh', status: false }
    ];

    const handleAddRoom = async (e) => {
        e.preventDefault();
        const token = localStorage.getItem('token');


        try {
            const response = await axios.post('http://localhost:8081/user/room', { roomName },
                { headers: { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' } }
            );
            alert(response.data.message || 'Room added successfully');
            setRoomName('');
            onRoomAdded();
            setShowAddRoomModal(false);
        } catch (error) {
            const err = error.response?.data?.error || 'Error adding room';
            alert(err);
            setRoomName('');
            setShowAddRoomModal(false);
        }
    };

    return (
        <>
            <div className="container px-5 py-4">
                <div className="d-flex justify-content-between align-items-center mb-3">
                    <div style={{ fontSize: '24px', lineHeight: '100%', letterSpacing: '0' }}>Living Room</div>
                    <button onClick={() => setShowAddRoomModal(true)} className="btn btn-dark">Add Room</button>
                </div>

                <div className="table-responsive">
                    <table className="table align-middle border-0">
                        <thead className="table-1C1C1E border-0">
                            <tr>
                                <th style={{ fontWeight: '700', fontSize: '16px', lineHeight: '100%', letterSpacing: '-0.39px' }} className='p-3 border-0'>Devices</th>
                                <th style={{ fontWeight: '700', fontSize: '16px', lineHeight: '100%', letterSpacing: '-0.39px' }} className='p-3 border-0'>Room</th>
                                <th style={{ fontWeight: '700', fontSize: '16px', lineHeight: '100%', letterSpacing: '-0.39px' }} className='p-3 border-0'>Power consumption</th>
                                <th style={{ fontWeight: '700', fontSize: '16px', lineHeight: '100%', letterSpacing: '-0.39px' }} className='p-3 border-0'>Status</th>
                            </tr>
                        </thead>
                        <tbody>
                            {devices.map((ref, index) => (
                                <tr key={index} className={index % 2 === 0 ? 'table-EAEAEA' : ''}>
                                    <td style={{ fontWeight: '600', fontSize: '14px', lineHeight: '100%', letterSpacing: '-0.39px' }} className='p-3 border-0'>
                                        {ref.name}
                                    </td>
                                    <td style={{ fontWeight: '600', fontSize: '14px', lineHeight: '100%', letterSpacing: '-0.39px' }} className='p-3 border-0'>
                                        {ref.room}
                                    </td>
                                    <td style={{ fontWeight: '600', fontSize: '14px', lineHeight: '100%', letterSpacing: '-0.39px' }} className='p-3 border-0'>{ref.power}</td>
                                    <td style={{ fontWeight: '600', fontSize: '14px', lineHeight: '100%', letterSpacing: '-0.39px' }} className='p-3 border-0'>
                                        <div className="form-check form-switch d-flex align-items-center">
                                            <input className="form-check-input" type="checkbox" checked={ref.status} readOnly />
                                        </div>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            </div>

            {showAddRoomModal && (
                <div style={{
                    position: "fixed", top: 0, left: 0, right: 0, bottom: 0,
                    backgroundColor: "rgba(0,0,0,0.5)", display: "flex",
                    alignItems: "center", justifyContent: "center", zIndex: 1050
                }}>
                    <div style={{
                        backgroundColor: "#fff", borderRadius: "20px", width: "100%",
                        maxWidth: "440px", boxShadow: "0 10px 25px rgba(0,0,0,0.2)",
                        padding: "24px", position: "relative", textAlign: "center"
                    }}>
                        <button onClick={() => setShowAddRoomModal(false)} style={{
                            position: "absolute", top: "8px", right: "12px",
                            background: 'none', fontSize: '24px', cursor: "pointer"
                        }} className='border-0'>&times;</button>

                        <div style={{ fontWeight: 600, fontSize: '24px' }} className='mb-3'>Add Room</div>

                        <form onSubmit={handleAddRoom}>
                            <div className="mb-3 text-start">
                                <label className="form-label">Room Name</label>
                                <input type="text" className="form-control"
                                    value={roomName} onChange={(e) => setRoomName(e.target.value)} />
                            </div>

                            <div className="d-flex justify-content-around">
                                <button type="button" onClick={() => setShowAddRoomModal(false)} className='btn btn-outline-dark px-4'>Cancel</button>
                                <button type="submit" className='btn btn-dark px-4'>Save</button>
                            </div>
                        </form>
                    </div>
                </div>
            )}

        </>
    );
};