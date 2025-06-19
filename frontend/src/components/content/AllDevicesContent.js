import { useState, useEffect, useCallback } from 'react';
import { FaTrash } from 'react-icons/fa';
import { useNavigate, Link } from 'react-router-dom';
import axios from 'axios';
import ModalLayout from '../layout/ModalLayout';

const customStyle1 = { fontWeight: '700', fontSize: '16px', lineHeight: '100%', letterSpacing: '-0.39px' };
const customStyle2 = { fontWeight: '600', fontSize: '14px', lineHeight: '100%', letterSpacing: '-0.39px', height: '60px' };

export default function AllDevicesContent() {
    const [devices, setDevices] = useState([]);
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
                const filteredDevices = data.things.map(thingsObj => {
                    const mainItem = thingsObj.items?.find(itemsObj =>
                        itemsObj.type === 'Switch' || itemsObj.type === 'Color' || itemsObj.name?.toLowerCase().includes('wiz_bulb_color')) || thingsObj.items?.[0];
                    return {
                        label: thingsObj.label,
                        itemName: mainItem?.name,
                        roomName: thingsObj.roomName,
                        status: mainItem?.state === 'ON' || mainItem?.state !== '0,0,0',
                        thingUID: thingsObj.thingUID
                    };
                });
                setDevices(filteredDevices);
            }
        } catch (err) {
            setDevices([]);
            const errorMessage = err.response?.data?.error || 'Failed to fetch device';
            console.error(errorMessage);
        }
    }, [token, navigate]);

    useEffect(() => {
        fetchDevice();
        const intervalId = setInterval(() => {
            fetchDevice();
        }, 3000);
        return () => clearInterval(intervalId);
    }, [fetchDevice]);

    const handleToggle = async (itemName, newStatus) => {
        if (!token) {
            navigate('/');
            return;
        }
        try {
            const { status } = await axios.post(`${process.env.REACT_APP_API_URL}/user/control`,
                { itemName, command: newStatus ? 'ON' : 'OFF' },
                { headers: { Authorization: `Bearer ${token}` } }
            );
            if (status === 200) {
                fetchDevice();
            }
        } catch (err) {
            const errorMessage = err.response?.data?.error || 'Sending command failed';
            console.error(errorMessage);
        }
    };

    const handleDelete = async (thingUID) => {
        if (!token) {
            navigate('/');
            return;
        }
        if (!window.confirm('Are you sure you want to delete this device?')) return;
        try {
            const { data, status } = await axios.delete(`${process.env.REACT_APP_API_URL}/user/delete/thing`,
                { headers: { Authorization: `Bearer ${token}` }, params: { thingUID } }
            );
            if (status === 200) {
                setModal({
                    show: true,
                    title: 'Success',
                    message: data.message,
                    isError: false,
                    onConfirm: () => { setModal({ ...modal, show: false }); }
                });
                fetchDevice();
            }
        } catch (err) {
            const errorMessage = err.response?.data?.error || 'Failed to delete device';
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

                <div className='d-flex justify-content-between align-items-center mb-3'>
                    <div style={{ fontSize: '24px', lineHeight: '100%', letterSpacing: '0' }}>All Devices</div>
                    <Link className='btn btn-dark' to={'/devices/search_bindings'} >Add Device</Link>
                </div>

                {/* Table */}
                <div style={{ width: '100%', overflowX: 'hidden', height: '300px', overflowY: 'auto' }}>
                    {devices.length > 0 ? (
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
                                    {devices.map((devicesObj, index) => (
                                        <tr key={index} className={index % 2 === 0 ? 'table-EAEAEA' : ''}>
                                            <td className='p-3 border-0' style={{ ...customStyle2 }}>
                                                {devicesObj.label}
                                            </td>
                                            <td className='p-3 border-0' style={{ ...customStyle2 }}>
                                                {devicesObj.roomName}
                                            </td>
                                            <td className='p-3 border-0' style={{ ...customStyle2 }}>
                                                <div className='form-check form-switch d-flex align-items-center'>
                                                    <input
                                                        style={{ cursor: 'pointer' }}
                                                        className='form-check-input'
                                                        type='checkbox'
                                                        checked={devicesObj.status}
                                                        onChange={() => handleToggle(devicesObj.itemName, !devicesObj.status)}
                                                    />
                                                </div>
                                            </td>
                                            <td className='p-3 border-0' style={{ ...customStyle2 }}>
                                                <FaTrash style={{ fontSize: '16px', cursor: 'pointer' }}
                                                    onClick={() => handleDelete(devicesObj.thingUID)}
                                                />
                                            </td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        </div>
                    ) : (
                        <div className='alert d-flex justify-content-center align-items-center h-100'>No devices found</div>
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