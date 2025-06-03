import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";
import React from "react";

export default function SearchBindingsContent() {
    const [isOpen, setIsOpen] = useState(false);
    const [selectedBinding, setSelectedBinding] = useState('');
    const [inbox, setInbox] = useState([]);
    const [rooms, setRooms] = useState([]);
    const [selectedRoom, setSelectedRoom] = useState('');
    const [customLabels, setCustomLabels] = useState({});
    const navigate = useNavigate();

    const bindings = ['Wiz', 'Astro', 'Z-Wave', 'Nest'];

    useEffect(() => {
        const fetchRooms = async () => {
            try {
                const token = localStorage.getItem('token');
                const response = await axios.get('http://localhost:8081/user/room', {
                    headers: { Authorization: `Bearer ${token}` }
                });
                setRooms(response.data || []);
            } catch (err) {
                console.error("Failed to load rooms", err);
            }
        };
        fetchRooms();
    }, []);

    const handleSelect = async (binding) => {
        setSelectedBinding(binding);
        setIsOpen(false);
        try {
            const token = localStorage.getItem('token');
            const response = await axios.post(
                `http://localhost:8081/user/scan?binding=${binding.toLowerCase()}`, {},
                { headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` } }
            );
            if (response.status === 200) {
                setInbox(response.data);
                alert('Scan completed and inbox updated.');
            }
        } catch (err) {
            const error = err.response?.data?.error || "Unknown error";
            console.error("Scan and inbox error:", error);
        }
    };

    const handleLabelChange = (index, value) => {
        setCustomLabels(prev => ({ ...prev, [index]: value }));
    };

    const handleAddDevice = async (device, index) => {
        if (!selectedRoom) {
            alert("Please select a room first.");
            return;
        }

        try {
            const token = localStorage.getItem('token');

            const ipRegex = /(\d{1,3}\.){3}\d{1,3}/;
            const extractedIP = device.label.match(ipRegex)?.[0] || '';

            const payload = {
                thingTypeUID: selectedBinding.toLowerCase() === 'wiz' ? 'wiz:color-bulb' : '',
                label: customLabels[index] || device.label,
                roomName: selectedRoom,
                host: extractedIP,
                macAddress: device.host || '',
            };

            const response = await axios.post(
                'http://localhost:8081/user/thing', payload,
                { headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` } }
            );

            if (response.status === 200) {
                alert("Device added successfully.");
                navigate('/devices/all_devices');
            }
        } catch (err) {
            const error = err.response?.data?.error || "Failed to add device";
            alert(error);
        }
    };

    return (
        <div className="container px-5 py-4">
            <div style={{ fontSize: '24px', lineHeight: '100%', letterSpacing: '0' }} className='mb-3'>
                Search Bindings
            </div>

            <div style={{ height: '300px', overflowY: 'auto' }}>
                <div className='mb-3'>
                    <label className="form-label fw-bold">Bindings</label>
                    <div className="position-relative">
                        <button type="button" onClick={() => setIsOpen(!isOpen)} className="form-select text-start py-2">
                            {selectedBinding || '--Select--'}
                        </button>

                        {isOpen && (
                            <ul className="list-group position-absolute mt-1 w-100 shadow" style={{ zIndex: 10, maxHeight: '200px', overflowY: 'auto' }}>
                                {bindings.map((ref, index) => (
                                    <React.Fragment key={index}>
                                        <li className="list-group-item list-group-item-action" style={{ cursor: 'pointer' }} onClick={() => handleSelect(ref)}>
                                            {ref}
                                        </li>
                                    </React.Fragment>
                                ))}
                            </ul>
                        )}
                    </div>
                </div>

                {/* Room selection dropdown */}
                <div className='mb-3'>
                    <label className="form-label fw-bold">Select Room</label>
                    <select className="form-select py-2" value={selectedRoom} onChange={(e) => setSelectedRoom(e.target.value)}>
                        <option value="">-- Select Room --</option>
                        {rooms.map(room => (
                            <option key={room.id} value={room.roomName}>{room.roomName}</option>
                        ))}
                    </select>
                </div>

                {inbox.length > 0 && <div className="fw-bold">{selectedBinding}</div>}

                {inbox.length > 0 && (
                    inbox.map((device, index) => (
                        <li key={index} className="list-group-item">
                            <div className="d-flex justify-content-between align-items-center">
                                <div className="flex-grow-1 me-3">
                                    <input
                                        type="text"
                                        className="form-control py-2"
                                        value={customLabels[index] || device.label}
                                        onChange={(e) => handleLabelChange(index, e.target.value)}
                                        autoFocus={true}
                                    />
                                </div>
                                <button className="btn btn-sm btn-dark" onClick={() => handleAddDevice(device, index)}>
                                    Add Device
                                </button>
                            </div>
                        </li>
                    ))
                )}
            </div>
        </div >
    );
};