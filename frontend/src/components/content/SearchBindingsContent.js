import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import React from 'react';
import ModalLayout from '../layout/ModalLayout';

export default function SearchBindingsContent() {
    const [rooms, setRooms] = useState([]);
    const [isOpen, setIsOpen] = useState(false);
    const [selectedBinding, setSelectedBinding] = useState('');
    const [inbox, setInbox] = useState([]);
    const [selectedRoom, setSelectedRoom] = useState('');
    const [customLabels, setCustomLabels] = useState({});
    const [modal, setModal] = useState({ show: false, title: '', message: '', isError: false, onConfirm: null });
    const navigate = useNavigate();

    const bindings = ['Wiz', 'Astro', 'Z-Wave', 'Nest'];

    const token = localStorage.getItem('token');

    useEffect(() => {
        if (!token) {
            navigate('/');
            return;
        }
        const fetchRoom = async () => {
            try {
                const { data, status } = await axios.get(`${process.env.REACT_APP_API_URL}/user/room`,
                    { headers: { Authorization: `Bearer ${token}` } }
                );
                if (status === 200) {
                    setRooms(data);
                }
            } catch (err) {
                setRooms([]);
                const errorMessage = err.response?.data?.error || 'Device fetching failed';
                console.error(errorMessage);
            }
        };
        fetchRoom();
    }, [navigate, token]);

    const handleSelect = async (binding) => {
        if (!token) {
            navigate('/');
            return;
        }
        setSelectedBinding(binding);
        setIsOpen(false);
        try {
            const { data, status } = await axios.post(`${process.env.REACT_APP_API_URL}/user/scan?binding=${binding.toLowerCase()}`, {},
                { headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` } }
            );
            if (status === 200) {
                setInbox(data);
                setModal({
                    show: true,
                    title: 'Success',
                    message: 'Scanning completed and Inbox updated successfully',
                    isError: false,
                    onConfirm: () => { setModal({ ...modal, show: false }); }
                });
            }
        } catch (err) {
            const errorMessage = err.response?.data?.error || 'Scanning or Inbox updation failed';
            setModal({
                show: true,
                title: 'Failed',
                message: <span className='text-danger'>{errorMessage}</span>,
                isError: true,
                onConfirm: () => setModal({ ...modal, show: false }),
            });
        }
    };

    const handleLabelChange = (index, value) => {
        setCustomLabels(prev => ({ ...prev, [index]: value }));
    };

    const handleAddDevice = async (device, index) => {
        if (!token) {
            navigate('/');
            return;
        }
        if (!selectedRoom) {
            setModal({
                show: true,
                title: 'Error',
                message: <span className='text-danger'>Please select a room</span>,
                isError: true,
                onConfirm: () => setModal({ ...modal, show: false }),
            });
            return;
        }
        try {
            const ipRegex = /(\d{1,3}\.){3}\d{1,3}/;
            const extractedIP = device.label.match(ipRegex)?.[0] || '';

            const payload = {
                thingTypeUID: selectedBinding.toLowerCase() === 'wiz' ? 'wiz:color-bulb' : '',
                label: customLabels[index] || device.label,
                roomName: selectedRoom,
                host: extractedIP,
                macAddress: device.host || '',
            };

            const { status } = await axios.post(`${process.env.REACT_APP_API_URL}/user/thing`, payload,
                { headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` } }
            );
            if (status === 200) {
                setModal({
                    show: true,
                    title: 'Success',
                    message: 'Device added successfully',
                    isError: false,
                    onConfirm: () => {
                        setModal({ ...modal, show: false });
                        navigate('/devices/all_devices');
                    }
                });
            }
        } catch (err) {
            const errorMessage = err.response?.data?.error || 'Failed to add device';
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
            <div className='container px-5 py-4'>

                <div style={{ fontSize: '24px', lineHeight: '100%', letterSpacing: '0' }} className='mb-3'>Search Bindings</div>

                <div style={{ width: '100%', overflowX: 'hidden', height: '300px', overflowY: 'auto' }}>

                    {/* Binding selection dropdown */}
                    <div className='mb-3'>

                        <div className='form-label fw-bold'>Bindings</div>

                        <div className='position-relative'>
                            <button type='button' onClick={() => setIsOpen(!isOpen)} className='form-select text-start py-2'>{selectedBinding || '--Select--'}</button>
                            {isOpen && (
                                <ul className='list-group position-absolute mt-1 w-100 shadow' style={{ zIndex: 10, maxHeight: '200px', overflowY: 'auto' }}>
                                    {bindings.map((bindingsObj, index) => (
                                        <React.Fragment key={index}>
                                            <li className='list-group-item list-group-item-action' style={{ cursor: 'pointer' }} onClick={() => handleSelect(bindingsObj)}>
                                                {bindingsObj}
                                            </li>
                                        </React.Fragment>
                                    ))}
                                </ul>
                            )}
                        </div>

                    </div>

                    {/* Room selection dropdown */}
                    <div className='mb-3'>

                        <div className='form-label fw-bold'>Select Room</div>

                        <select className='form-select py-2' id='room' value={selectedRoom} onChange={(e) => setSelectedRoom(e.target.value)}>
                            <option value=''>-- Select Room --</option>
                            {rooms.map((roomObj, index) => (
                                <option key={index} value={roomObj.roomName}>{roomObj.roomName}</option>
                            ))}
                        </select>

                    </div>

                    {inbox.length > 0 && <div className='fw-bold'>{selectedBinding}</div>}

                    {inbox.length > 0 && (
                        inbox.map((inboxObj, index) => (
                            <React.Fragment key={index}>
                                <li className='list-group-item'>
                                    <div className='d-flex justify-content-between align-items-center'>
                                        <div className='flex-grow-1 me-3'>
                                            <input
                                                id='label'
                                                type='text'
                                                className='form-control py-2'
                                                value={customLabels[index] || inboxObj.label}
                                                onChange={(e) => handleLabelChange(index, e.target.value)}
                                                autoFocus={true}
                                            />
                                        </div>
                                        <button className='btn btn-sm btn-dark' onClick={() => handleAddDevice(inboxObj, index)}>
                                            Add Device
                                        </button>
                                    </div>
                                </li>
                            </React.Fragment>
                        ))
                    )}
                </div>
            </div>

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